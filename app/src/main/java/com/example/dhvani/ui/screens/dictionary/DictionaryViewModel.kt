package com.example.dhvani.ui.screens.dictionary

import androidx.lifecycle.ViewModel
import com.example.dhvani.data.model.SignItem
import com.example.dhvani.data.repository.SignRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val repository: SignRepository
) : ViewModel() {

    private val _allSigns = MutableStateFlow<List<SignItem>>(emptyList())
    val allSigns = _allSigns.asStateFlow()

    init {
        _allSigns.value = repository.getAllSigns()
    }
}
