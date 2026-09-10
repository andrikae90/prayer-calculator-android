package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AppContainer
import com.example.ui.calendar.CalendarViewModel
import com.example.ui.calendar.HijriCalendarScreen
import com.example.ui.dua.DuaScreen
import com.example.ui.dua.DuaViewModel
import com.example.ui.dzikr.DzikrScreen
import com.example.ui.dzikr.DzikrViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.more.MoreScreen
import com.example.ui.prayer.PrayerScheduleScreen
import com.example.ui.prayer.PrayerViewModel
import com.example.ui.qibla.QiblaScreen
import com.example.ui.qibla.QiblaViewModel
import com.example.ui.quran.QuranScreen
import com.example.ui.quran.QuranViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.splash.SplashScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    container: AppContainer,
    onRefreshLocation: () -> Unit,
    isRefreshingLocation: Boolean = false,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isSplashScreen = currentRoute == Screen.Splash.route
    val isHomeScreen = currentRoute == Screen.Home.route
    val isBottomNavVisible = !isSplashScreen && (
            isHomeScreen ||
            currentRoute == Screen.Salat.route ||
            currentRoute == Screen.Quran.route ||
            currentRoute == Screen.More.route
    )

    val topBarTitle = when (currentRoute) {
        Screen.Home.route -> "Muslim Masan"
        Screen.Qibla.route -> "Arah Kiblat"
        Screen.Dua.route -> "Kumpulan Doa"
        Screen.Dzikr.route -> "Dzikir & Tasbih"
        Screen.Calendar.route -> "Kalender Hijriah"
        Screen.Settings.route -> "Pengaturan"
        else -> null
    }

    Scaffold(
        topBar = {
            if (topBarTitle != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        if (!isHomeScreen) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali"
                                )
                            }
                        }
                    },
                    actions = {
                        if (isHomeScreen) {
                            TextButton(
                                onClick = onRefreshLocation,
                                enabled = !isRefreshingLocation,
                                modifier = Modifier.testTag("refresh_location_button")
                            ) {
                                if (isRefreshingLocation) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .padding(2.dp)
                                            .testTag("location_refresh_progress"),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Memperbarui...",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Perbarui Lokasi",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Perbarui Lokasi",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        bottomBar = {
            if (isBottomNavVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    HomeViewModel(container.prayerRepository, container.settingsRepository)
                }
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToPrayer = { navController.navigate(Screen.Salat.route) },
                    onNavigateToQibla = { navController.navigate(Screen.Qibla.route) },
                    onNavigateToQuran = { navController.navigate(Screen.Quran.route) },
                    onNavigateToDua = { navController.navigate(Screen.Dua.route) },
                    onNavigateToDzikr = { navController.navigate(Screen.Dzikr.route) },
                    onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) }
                )
            }

            composable(Screen.Salat.route) {
                val prayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    PrayerViewModel(container.prayerRepository, container.settingsRepository)
                }
                PrayerScheduleScreen(viewModel = prayerViewModel)
            }

            composable(Screen.Quran.route) {
                val quranViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    QuranViewModel(container.quranRepository)
                }
                QuranScreen(viewModel = quranViewModel)
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToQibla = { navController.navigate(Screen.Qibla.route) },
                    onNavigateToDua = { navController.navigate(Screen.Dua.route) },
                    onNavigateToDzikr = { navController.navigate(Screen.Dzikr.route) },
                    onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Qibla.route) {
                val compassSensorManager = container.provideCompassSensorManager()
                val qiblaViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    QiblaViewModel(compassSensorManager, container.settingsRepository)
                }
                QiblaScreen(viewModel = qiblaViewModel)
            }

            composable(Screen.Dua.route) {
                val duaViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    DuaViewModel(container.duaRepository)
                }
                DuaScreen(viewModel = duaViewModel)
            }

            composable(Screen.Dzikr.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val dzikrViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    DzikrViewModel(container.dzikrRepository, context.applicationContext)
                }
                DzikrScreen(viewModel = dzikrViewModel)
            }

            composable(Screen.Calendar.route) {
                val calendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    CalendarViewModel(container.calendarRepository)
                }
                HijriCalendarScreen(viewModel = calendarViewModel)
            }

            composable(Screen.Settings.route) {
                val settingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    SettingsViewModel(container.settingsRepository)
                }
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
