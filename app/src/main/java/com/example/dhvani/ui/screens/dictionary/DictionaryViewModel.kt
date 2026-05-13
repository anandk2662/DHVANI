package com.example.dhvani.ui.screens.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.model.SignItem
import com.example.dhvani.data.repository.SignRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val repository: SignRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _expandedSignId = MutableStateFlow<String?>(null)
    val expandedSignId = _expandedSignId.asStateFlow()

    private val _allSigns = repository.getAllSigns()
    val allSigns = MutableStateFlow(_allSigns).asStateFlow()
    
    val filteredSigns: StateFlow<List<SignItem>> = _searchQuery
        .debounce(300) // Avoid filtering on every keystroke
        .combine(MutableStateFlow(_allSigns)) { query, signs ->
            if (query.isEmpty()) {
                signs
            } else {
                signs.filter { it.label.contains(query, ignoreCase = true) }
            }
        }
        .flowOn(kotlinx.coroutines.Dispatchers.Default) // Perform filtering off-thread
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _allSigns
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleExpansion(signId: String) {
        _expandedSignId.value = if (_expandedSignId.value == signId) null else signId
    }
}
