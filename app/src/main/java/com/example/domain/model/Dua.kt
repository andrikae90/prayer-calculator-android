package com.example.domain.model

enum class DuaCategory(val displayName: String, val iconDescription: String) {
    HARIAN("Doa Harian", "Doa kegiatan sehari-hari"),
    SETELAH_SALAT("Doa Setelah Salat", "Doa & wirid usai salat"),
    PERJALANAN("Doa Perjalanan", "Doa bepergian & musafir"),
    TIDUR("Doa Tidur", "Doa sebelum & sesudah tidur"),
    MAKAN("Doa Makan", "Doa adab makan & minum"),
    RAMADAN("Doa Ramadan", "Doa puasa & bulan Ramadan")
}

data class DuaItem(
    val id: String,
    val title: String,
    val category: DuaCategory,
    val arabic: String,
    val latin: String,
    val translation: String,
    val reference: String
)
