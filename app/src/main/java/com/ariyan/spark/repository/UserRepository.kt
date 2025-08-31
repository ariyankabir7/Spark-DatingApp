package com.ariyan.spark.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.ariyan.spark.model.MatchItem
import com.ariyan.spark.model.Message
import com.ariyan.spark.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // --- Authentication & Profile Management ---
    suspend fun signIn(email: String, password: String): String? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid
    }

    suspend fun signUp(email: String, password: String): String? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid
    }

    suspend fun uploadProfileImage(uid: String, fileUri: Uri): String {
        val ref = storage.reference.child("profile_images/$uid.jpg")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun createUserProfile(uid: String, user: User) {
        firestore.collection("users").document(uid).set(user).await()
    }

    suspend fun getUserProfile(uid: String): User? {
        val doc = firestore.collection("users").document(uid).get().await()
        if (!doc.exists()) return null
        return doc.toObject(User::class.java)?.copy(uid = doc.id)
    }

    suspend fun updateUserInterests(uid: String, interests: List<String>) {
        firestore.collection("users").document(uid).update("interests", interests).await()
    }

    suspend fun updateUserPresence() {
        val currentUid = getCurrentUserId() ?: return
        val update = mapOf("lastSeen" to Timestamp.now())
        firestore.collection("users").document(currentUid).update(update).await()
    }


    // --- Swiping and Matching Logic ---
    suspend fun getOtherUsers(currentUser: User): List<User> {
        val excludedIds = (currentUser.likes + currentUser.dislikes + currentUser.uid).distinct()

        val query = when (currentUser.gender.lowercase()) {
            "male" -> firestore.collection("users").whereEqualTo("gender", "female")
            "female" -> firestore.collection("users").whereEqualTo("gender", "male")
            else -> firestore.collection("users")
        }

        val snapshot = query.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
            if (user != null && user.uid !in excludedIds) user else null
        }
    }

    suspend fun saveSwipe(fromUserId: String, toUserId: String, action: String): Boolean {
        updateUserPresence()

        val swipeId = "${fromUserId}_$toUserId"
        val swipeData = mapOf(
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "action" to action,
            "timestamp" to Timestamp.now()
        )
        firestore.collection("matches").document(swipeId).set(swipeData).await()

        val userProfileRef = firestore.collection("users").document(fromUserId)
        if (action == "like") {
            userProfileRef.update("likes", FieldValue.arrayUnion(toUserId)).await()
        } else {
            userProfileRef.update("dislikes", FieldValue.arrayUnion(toUserId)).await()
        }

        if (action == "like") {
            val otherUserDoc = firestore.collection("users").document(toUserId).get().await()
            val otherUserLikes = otherUserDoc.get("likes") as? List<*>
            if (otherUserLikes?.contains(fromUserId) == true) {
                createMutualMatch(fromUserId, toUserId)
                return true
            }
        }
        return false
    }


    private suspend fun createMutualMatch(uid1: String, uid2: String) {
        val sorted = listOf(uid1, uid2).sorted()
        val matchId = "${sorted[0]}_${sorted[1]}"
        val data = mapOf(
            "participants" to sorted,
            "timestamp" to Timestamp.now()
        )
        firestore.collection("mutual_matches").document(matchId).set(data).await()
    }


    // --- Chat & Match List Logic (Real-time) ---
    suspend fun getOrCreateChatId(uid1: String, uid2: String): String {
        val chatId = getChatId(uid1, uid2)
        val data = hashMapOf(
            "chatId" to chatId,
            "participants" to listOf(uid1, uid2).sorted()
        )
        firestore.collection("chats").document(chatId).set(data, SetOptions.merge()).await()
        return chatId
    }

    suspend fun sendMessage(chatId: String, message: Message) {
        firestore.collection("chats").document(chatId)
            .collection("messages").add(message).await()

        val chatUpdates = mapOf(
            "lastMessage" to message.text,
            "lastMessageTimestamp" to message.timestamp
        )
        firestore.collection("chats").document(chatId).update(chatUpdates).await()
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val subscription = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObjects(Message::class.java)).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    fun getMatchesFlow(): Flow<List<MatchItem>> = callbackFlow {
        val currentUid = getCurrentUserId() ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        var latestUsersMap = emptyMap<String, User>()
        var latestChatsMap = emptyMap<String, com.google.firebase.firestore.DocumentSnapshot>()
        var usersListener: ListenerRegistration? = null

        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        var debounceJob: Job? = null

        fun combineAndSend() {
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(250) // A slightly longer, more robust delay

                if (latestUsersMap.isEmpty()) {
                    trySend(emptyList())
                    return@launch
                }

                val finalMatchItems = latestUsersMap.values.map { user ->
                    val chatId = getChatId(currentUid, user.uid)
                    val chatDoc = latestChatsMap[chatId]

                    MatchItem(
                        user = user,
                        lastMessage = chatDoc?.getString("lastMessage") ?: "Say hi! 👋",
                        lastMessageTime = chatDoc?.getTimestamp("lastMessageTimestamp")?.seconds,
                        lastSeen = user.lastSeen?.seconds
                    )
                }
                trySend(finalMatchItems.sortedByDescending { it.lastMessageTime ?: 0 })
            }
        }

        val chatsListener = firestore.collection("chats")
            .whereArrayContains("participants", currentUid)
            .addSnapshotListener { chatsSnapshot, error ->
                if (error != null) { return@addSnapshotListener }
                latestChatsMap = chatsSnapshot?.documents?.associateBy { it.id } ?: emptyMap()
                combineAndSend()
            }

        val matchesListener = firestore.collection("mutual_matches")
            .whereArrayContains("participants", currentUid)
            .addSnapshotListener { matchesSnapshot, error ->
                usersListener?.remove()

                if (error != null || matchesSnapshot == null) {
                    close(error ?: IllegalStateException("Matches snapshot is null"))
                    return@addSnapshotListener
                }

                val matchedUserIds = matchesSnapshot.documents.mapNotNull {
                    val participants = it.get("participants") as? List<*>
                    participants?.firstOrNull { id -> id != currentUid } as? String
                }

                if (matchedUserIds.isEmpty()) {
                    latestUsersMap = emptyMap()
                    combineAndSend()
                    return@addSnapshotListener
                }

                usersListener = firestore.collection("users").whereIn(FieldPath.documentId(), matchedUserIds)
                    .addSnapshotListener { usersSnapshot, userError ->
                        if (userError != null || usersSnapshot == null) {
                            return@addSnapshotListener
                        }
                        latestUsersMap = usersSnapshot.documents.mapNotNull {
                            it.toObject(User::class.java)?.copy(uid = it.id)
                        }.associateBy { it.uid }
                        combineAndSend()
                    }
            }

        awaitClose {
            matchesListener.remove()
            chatsListener.remove()
            usersListener?.remove()
            scope.cancel()
        }
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    suspend fun doesUserExist(uid: String): Boolean {
        return try {
            firestore.collection("users").document(uid).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }
}

