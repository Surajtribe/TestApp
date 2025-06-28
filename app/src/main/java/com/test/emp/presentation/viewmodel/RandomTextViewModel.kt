package com.test.emp.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.emp.data.repository.RandomTextRepository
import com.test.emp.domain.model.RandomText
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RandomTextViewModel @Inject constructor(
    private val repository: RandomTextRepository
) : ViewModel() {

    private val _generatedStrings = MutableStateFlow<List<RandomText>>(emptyList())
    val generatedStrings = _generatedStrings.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    @RequiresApi(Build.VERSION_CODES.O)
    fun generateString(length: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val randomText = repository.getRandomText(length)
            if (randomText != null) {
                _generatedStrings.value = _generatedStrings.value + randomText
            } else {
                _error.value = "Failed to get random string"
            }
            _isLoading.value = false
        }
    }

    fun deleteAll() {
        _generatedStrings.value = emptyList()
    }

    fun deleteOne(randomText: RandomText) {
        _generatedStrings.value = _generatedStrings.value - randomText
    }

    fun clearError() {
        _error.value = null
    }
}
