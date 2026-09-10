package com.example.domain.model

enum class DzikrType(val displayName: String) {
    PAGI("Dzikir Pagi"),
    PETANG("Dzikir Petang"),
    SETELAH_SALAT("Dzikir Setelah Salat")
}

data class DzikrItem(
    val id: String,
    val title: String,
    val type: DzikrType,
    val arabic: String,
    val latin: String,
    val translation: String,
    val repeatTarget: Int = 1,
    val note: String = ""
)

data class TasbihState(
    val count: Int = 0,
    val target: Int = 33,
    val totalRounds: Int = 0,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = false
)
