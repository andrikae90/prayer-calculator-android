package com.example.domain.model

enum class RevelationType(val displayName: String) {
    MAKKIYAH("Makkiyah"),
    MADANIYAH("Madaniyah")
}

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameLatin: String,
    val translation: String,
    val ayahCount: Int,
    val revelationType: RevelationType
)
