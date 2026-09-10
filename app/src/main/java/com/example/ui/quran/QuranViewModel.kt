package com.example.ui.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.QuranAyahRepository
import com.example.data.repository.QuranRepository
import com.example.domain.model.QuranAyah
import com.example.domain.model.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuranViewModel(
    private val quranRepository: QuranRepository,
    private val ayahRepository: QuranAyahRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val surahs: StateFlow<List<Surah>> = _searchQuery
        .flatMapLatest { query -> quranRepository.searchSurahs(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _ayahs = MutableStateFlow<List<QuranAyah>>(emptyList())
    val ayahs: StateFlow<List<QuranAyah>> = _ayahs.asStateFlow()

    private val _isLoadingAyahs = MutableStateFlow(false)
    val isLoadingAyahs: StateFlow<Boolean> = _isLoadingAyahs.asStateFlow()

    private val _ayahError = MutableStateFlow<String?>(null)
    val ayahError: StateFlow<String?> = _ayahError.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectSurah(surah: Surah) {
        _selectedSurah.value = surah
        _ayahs.value = emptyList()
        _ayahError.value = null
        _isLoadingAyahs.value = true
        viewModelScope.launch {
            val result = ayahRepository.getAyahs(surah.number)
            result.onSuccess { verses ->
                _ayahs.value = verses
                if (verses.isEmpty()) _ayahError.value = "Ayat belum tersedia. Periksa koneksi internet."
            }.onFailure {
                _ayahError.value = "Gagal memuat ayat. Periksa koneksi internet lalu coba lagi."
            }
            _isLoadingAyahs.value = false
        }
    }

    fun retryAyahs() {
        _selectedSurah.value?.let { selectSurah(it) }
    }

    fun clearSelectedSurah() {
        _selectedSurah.value = null
        _ayahs.value = emptyList()
        _ayahError.value = null
    }
}
