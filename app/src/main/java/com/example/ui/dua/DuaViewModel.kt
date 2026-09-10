package com.example.ui.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DuaRepository
import com.example.domain.model.DuaCategory
import com.example.domain.model.DuaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class DuaViewModel(
    private val duaRepository: DuaRepository
) : ViewModel() {

    val categories: List<DuaCategory> = duaRepository.getCategories()

    private val _selectedCategory = MutableStateFlow(DuaCategory.HARIAN)
    val selectedCategory: StateFlow<DuaCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val duas: StateFlow<List<DuaItem>> = _selectedCategory
        .flatMapLatest { category ->
            duaRepository.getDuasByCategory(category)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCategory(category: DuaCategory) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
