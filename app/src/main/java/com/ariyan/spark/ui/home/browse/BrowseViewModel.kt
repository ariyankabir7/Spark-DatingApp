package com.ariyan.spark.ui.home.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.model.User
import com.ariyan.spark.repository.UserRepository
import com.ariyan.spark.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class BrowseViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _otherUsers = MutableLiveData<List<User>>()
    val otherUsers: LiveData<List<User>> = _otherUsers

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _mutualMatch = SingleLiveEvent<User?>()
    val mutualMatch: LiveData<User?> = _mutualMatch

    fun loadUsers() {
        if (otherUsers.value?.isNotEmpty() == true) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = repository.getCurrentUserId()
                if (uid != null) {
                    val user = repository.getUserProfile(uid)
                    _currentUser.value = user
                    if (user != null) {
                        val list = repository.getOtherUsers(user)
                        _otherUsers.value = list.shuffled()
                    } else {
                        _error.value = "Current user profile not found"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error loading users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSwipe(toUserId: String, action: String) {
        val fromUserId = repository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val isMutual = repository.saveSwipe(fromUserId, toUserId, action)
                if (isMutual) {
                    val matchedUser = repository.getUserProfile(toUserId)
                    _mutualMatch.postValue(matchedUser)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * NEW: Removes a user from the local browsing list after a match.
     * This ensures they don't reappear after the match dialog is shown.
     */
    fun removeUserFromStack(userId: String) {
        val currentList = _otherUsers.value ?: return
        // Create a new list excluding the matched user.
        val newList = currentList.filterNot { it.uid == userId }
        _otherUsers.value = newList
    }

    fun updateUserPresence() {
        viewModelScope.launch {
            try {
                repository.updateUserPresence()
            } catch (e: Exception) {
                // Presence updates can fail silently
            }
        }
    }
}

