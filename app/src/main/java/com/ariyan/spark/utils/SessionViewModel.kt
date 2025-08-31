package com.ariyan.spark.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository()

    // This event will be observed by the BaseFragment to trigger navigation
    private val _forceLogoutEvent = SingleLiveEvent<String>()
    val forceLogoutEvent: LiveData<String> = _forceLogoutEvent

    /**
     * Checks the status of the current user's auth account (e.g., if it's disabled).
     * And checks if the user's profile document still exists in Firestore.
     */
    fun checkSessionStatus() {
        val user = auth.currentUser
        if (user == null) {
            _forceLogoutEvent.postValue("You have been logged out.")
            return
        }

        // Check if user is disabled in Firebase Auth
        user.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                val exceptionMessage = task.exception?.message ?: ""
                if (exceptionMessage.contains("USER_DISABLED", ignoreCase = true)) {
                    _forceLogoutEvent.postValue("Your account has been disabled by an administrator.")
                }
            } else {
                 // If the user is not disabled, check if their Firestore profile exists
                viewModelScope.launch {
                    if (!repository.doesUserExist(user.uid)) {
                        _forceLogoutEvent.postValue("Your user profile was not found.")
                    }
                }
            }
        }
    }
}
