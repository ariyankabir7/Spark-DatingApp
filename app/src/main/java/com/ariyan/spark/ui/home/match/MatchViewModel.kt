package com.ariyan.spark.ui.home.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.model.MatchItem
import com.ariyan.spark.repository.UserRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _matches = MutableLiveData<List<MatchItem>>()
    val matches: LiveData<List<MatchItem>> = _matches

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // THE FIX: Add a flag to ensure the loading indicator is only hidden once.
    private var hasLoadedOnce = false

    init {
        listenForMatches()
    }

    private fun listenForMatches() {
        _isLoading.value = true
        hasLoadedOnce = false // Reset on new listen
        viewModelScope.launch {
            repository.getMatchesFlow()
                .catch { e ->
                    _error.postValue("Error fetching matches: ${e.message}")
                    _isLoading.postValue(false)
                }
                .collect { matchList ->
                    _matches.postValue(matchList)
                    // Only set loading to false after the very first data emission.
                    if (!hasLoadedOnce) {
                        _isLoading.postValue(false)
                        hasLoadedOnce = true
                    }
                }
        }
    }
}
