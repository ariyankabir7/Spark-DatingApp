package com.ariyan.spark.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.repository.UserRepository
import com.ariyan.spark.utils.NavDestination
import kotlinx.coroutines.launch

class SplashViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    val navigation = MutableLiveData<NavDestination>()

    fun checkNavigation() {
        viewModelScope.launch {
            val uid = repo.getCurrentUserId()
            if (uid == null) {
                navigation.postValue(NavDestination.AUTH)
                return@launch
            }

            val user = try { repo.getUserProfile(uid) } catch (e: Exception) { null }
            when {
                user == null -> navigation.postValue(NavDestination.AUTH)
                user.interests.isEmpty() -> navigation.postValue(NavDestination.INTERESTS)
                else -> navigation.postValue(NavDestination.HOME)
            }
        }
    }
}
