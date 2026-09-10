package com.example.domain.model

data class DailyReminder(
    val id: Int,
    val surahName: String,
    val reference: String,
    val arabicText: String,
    val translation: String,
    val category: String = "Ayat Hari Ini"
)
