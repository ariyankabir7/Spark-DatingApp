package com.ariyan.spark.ui.home.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.model.User
import com.ariyan.spark.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _updateSuccess = MutableLiveData<Boolean>(false)
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    fun loadUserProfile() {
        val uid = repository.getCurrentUserId() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _user.value = repository.getUserProfile(uid)
            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(name: String, age: Int, gender: String, photoUri: Uri?) {
        val uid = repository.getCurrentUserId() ?: return
        val currentUser = _user.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                var photoUrl = currentUser.photoUrl
                // If a new photo is selected, upload it first
                if (photoUri != null) {
                    photoUrl = repository.uploadProfileImage(uid, photoUri)
                }

                val updatedUser = currentUser.copy(
                    name = name,
                    age = age,
                    gender = gender.lowercase(),
                    photoUrl = photoUrl
                )
                // Use the existing createUserProfile which also works for updates (set with merge)
                repository.createUserProfile(uid, updatedUser)
                _user.value = updatedUser // Update local data
                _updateSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
