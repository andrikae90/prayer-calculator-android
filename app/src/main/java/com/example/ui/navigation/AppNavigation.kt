package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.data.AppContainer
import com.example.ui.calendar.*
import com.example.ui.dua.*
import com.example.ui.dzikr.*
import com.example.ui.home.*
import com.example.ui.more.MoreScreen
import com.example.ui.prayer.*
import com.example.ui.qibla.*
import com.example.ui.quran.*
import com.example.ui.settings.*
import com.example.ui.splash.SplashScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(container: AppContainer, onRefreshLocation: () -> Unit, isRefreshingLocation: Boolean = false, navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isSplashScreen = currentRoute == Screen.Splash.route
    val isHomeScreen = currentRoute == Screen.Home.route
    val isBottomNavVisible = !isSplashScreen && (isHomeScreen || currentRoute == Screen.Salat.route || currentRoute == Screen.Quran.route || currentRoute == Screen.More.route)
    val topBarTitle = when (currentRoute) {
        Screen.Home.route -> "TEMAN SHOLAT"
        Screen.Qibla.route -> "Arah Kiblat"
        Screen.Dua.route -> "Kumpulan Doa"
        Screen.Dzikr.route -> "Dzikir & Tasbih"
        Screen.Calendar.route -> "Kalender Hijriah"
        Screen.Settings.route -> "Pengaturan"
        else -> null
    }
    Scaffold(
        topBar = { if (topBarTitle != null) TopAppBar(
            title = { Text(topBarTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            navigationIcon = { if (!isHomeScreen) IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") } },
            actions = { if (isHomeScreen) TextButton(onClick = onRefreshLocation, enabled = !isRefreshingLocation, modifier = Modifier.testTag("refresh_location_button")) {
                if (isRefreshingLocation) { CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).padding(2.dp), strokeWidth = 2.dp); Text("Memperbarui...") }
                else { Icon(Icons.Filled.Refresh, contentDescription = "Perbarui Lokasi", modifier = Modifier.padding(end = 8.dp)); Text("Perbarui Lokasi") }
            } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.primary, actionIconContentColor = MaterialTheme.colorScheme.primary)
        ) },
        bottomBar = { if (isBottomNavVisible) NavigationBar(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag("main_bottom_nav")) {
            bottomNavItems.forEach { item -> val selected = currentRoute == item.route; NavigationBarItem(selected = selected, onClick = { if (!selected) navController.navigate(item.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }, icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = item.title) }, label = { Text(item.title) }, modifier = Modifier.testTag("nav_item_${item.route}")) }
        } }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Splash.route, modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            composable(Screen.Splash.route) { SplashScreen(onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }) }
            composable(Screen.Home.route) { val vm = androidx.lifecycle.viewmodel.compose.viewModel { HomeViewModel(container.prayerRepository, container.settingsRepository) }; HomeScreen(vm, { navController.navigate(Screen.Salat.route) }, { navController.navigate(Screen.Qibla.route) }, { navController.navigate(Screen.Quran.route) }, { navController.navigate(Screen.Dua.route) }, { navController.navigate(Screen.Dzikr.route) }, { navController.navigate(Screen.Calendar.route) }) }
            composable(Screen.Salat.route) { val vm = androidx.lifecycle.viewmodel.compose.viewModel { PrayerViewModel(container.prayerRepository, container.settingsRepository) }; PrayerScheduleScreen(vm) }
            composable(Screen.Quran.route) { val vm = androidx.lifecycle.viewmodel.compose.viewModel { QuranViewModel(container.quranRepository, container.quranAyahRepository) }; QuranScreen(vm) }
            composable(Screen.More.route) { MoreScreen({ navController.navigate(Screen.Qibla.route) }, { navController.navigate(Screen.Dua.route) }, { navController.navigate(Screen.Dzikr.route) }, { navController.navigate(Screen.Calendar.route) }, { navController.navigate(Screen.Settings.route) }) }
            composable(Screen.Qibla.route) { val sensor = container.provideCompassSensorManager(); val vm = androidx.lifecycle.viewmodel.compose.viewModel { QiblaViewModel(sensor, container.settingsRepository) }; QiblaScreen(vm) }
            composable(Screen.Dua.route) { val vm = androidx.lifecycle.viewmodel.compose.viewModel { DuaViewModel(container.duaRepository) }; DuaScreen(vm) }
            composable(Screen.Dzikr.route) { val context = androidx.compose.ui.platform.LocalContext.current; val vm = androidx.lifecycle.viewmodel.compose.viewModel { DzikrViewModel(container.dzikrRepository, context.applicationContext) }; DzikrScreen(vm) }
            composable(Screen.Calendar.route) { val vm = androidx.lifecycle.viewmodel.compose.viewModel { CalendarViewModel(container.calendarRepository) }; HijriCalendarScreen(vm) }
            composable(Screen.Settings.route) { val vm = androidx.lifecycle.viewmodel.compose.viewModel { SettingsViewModel(container.settingsRepository, container.locationProvider) }; EnhancedSettingsScreen(vm) }
        }
    }
}
