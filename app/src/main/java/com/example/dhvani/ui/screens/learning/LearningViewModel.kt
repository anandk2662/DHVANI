package com.example.dhvani.ui.screens.learning

import androidx.lifecycle.ViewModel
import com.example.dhvani.data.model.SignCategory
import com.example.dhvani.data.model.SignItem
import com.example.dhvani.data.repository.SignRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val repository: SignRepository
) : ViewModel() {

    private val _signs = MutableStateFlow<List<SignItem>>(emptyList())
    val signs = _signs.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    fun loadCategory(category: SignCategory) {
        _signs.value = repository.getSignsByCategory(category)
        _currentIndex.value = 0
    }

    fun next() {
        if (_currentIndex.value < _signs.value.size - 1) {
            _currentIndex.value++
        }
    }

    fun previous() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
        }
    }
}
