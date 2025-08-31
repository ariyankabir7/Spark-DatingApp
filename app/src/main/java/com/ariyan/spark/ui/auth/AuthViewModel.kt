package com.ariyan.spark.ui.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.model.User
import com.ariyan.spark.repository.UserRepository
import com.ariyan.spark.utils.SingleLiveEvent
import kotlinx.coroutines.launch

sealed class AuthResult {
    data class Success(val hasInterests: Boolean) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Use a SingleLiveEvent to ensure navigation happens only once
    private val _authResult = SingleLiveEvent<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    fun signIn(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val uid = repository.signIn(email, password)
                if (uid != null) {
                    checkUserInterests(uid)
                } else {
                    _authResult.postValue(AuthResult.Error("Login failed"))
                }
            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.message ?: "An unknown error occurred"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, name: String, age: Int, gender: String, photoUri: Uri?) {
        if (photoUri == null) {
            _authResult.value = AuthResult.Error("Profile picture is required")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Step 1: Create the Auth user
                val uid = repository.signUp(email, password) ?: throw Exception("Signup failed")

                val user = User(uid = uid, name = name, age = age, gender = gender)

                // Step 2: Create initial profile in Firestore (this is crucial for Storage rules)
                repository.createUserProfile(uid, user)
                // --- DEBUGGING STEP ---
                // Log the exact values being used. You may need to import android.util.Log.
                     val expectedFileName = "$uid.jpg"
                     android.util.Log.d("AuthDebug", "Uploading with UID: '$uid'")
                     android.util.Log.d("AuthDebug", "Expecting filename: '$expectedFileName'")
                // --------------------

                // Step 3: Upload the profile image
                val photoUrl = repository.uploadProfileImage(uid, photoUri)

                // Step 4: Update the profile with the final photoUrl
                repository.createUserProfile(uid, user.copy(photoUrl = photoUrl))

                _authResult.postValue(AuthResult.Success(hasInterests = false)) // New users never have interests

            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.message ?: "Signup failed"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun checkUserInterests(uid: String) {
        val user = repository.getUserProfile(uid)
        val hasInterests = !user?.interests.isNullOrEmpty()
        _authResult.postValue(AuthResult.Success(hasInterests))
    }
}
