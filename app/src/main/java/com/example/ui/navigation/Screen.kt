package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Salat : Screen("salat")
    object Quran : Screen("quran")
    object More : Screen("more")
    object Qibla : Screen("qibla")
    object Dua : Screen("dua")
    object Dzikr : Screen("dzikr")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Salat : BottomNavItem("salat", "Salat", Icons.Filled.Mosque, Icons.Outlined.Mosque)
    object Quran : BottomNavItem("quran", "Al-Qur'an", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object More : BottomNavItem("more", "Lainnya", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Salat,
    BottomNavItem.Quran,
    BottomNavItem.More
)
