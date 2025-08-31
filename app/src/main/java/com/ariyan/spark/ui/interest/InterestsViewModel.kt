package com.ariyan.spark.ui.interest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.repository.UserRepository
import kotlinx.coroutines.launch

class InterestsViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    val saveResult = MutableLiveData<Boolean>()
    val saveError = MutableLiveData<String?>()

    fun saveInterests(uid: String, interests: List<String>) {
        viewModelScope.launch {
            try {
                repo.updateUserInterests(uid, interests)
                saveResult.postValue(true)
            } catch (e: Exception) {
                saveResult.postValue(false)
                saveError.postValue(e.message)
            }
        }
    }
}
