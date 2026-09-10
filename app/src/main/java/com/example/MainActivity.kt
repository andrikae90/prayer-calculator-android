package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.AppThemeSetting
import com.example.domain.model.PrayerType
import com.example.ui.navigation.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private var isRefreshingLocation by mutableStateOf(false)

  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) refreshGpsLocation()
  }

  private val notificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) schedulePrayerNotifications()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val container = (application as MuslimApp).container

    setContent {
      val settings by container.settingsRepository.settingsState.collectAsState()
      val isSystemDark = isSystemInDarkTheme()
      val isDarkTheme = when (settings.themeSetting) {
        AppThemeSetting.SYSTEM -> isSystemDark
        AppThemeSetting.DARK -> true
        AppThemeSetting.LIGHT -> false
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        MainAppScaffold(
          container = container,
          onRefreshLocation = { requestLocationPermissionIfNeeded() },
          isRefreshingLocation = isRefreshingLocation
        )
      }
    }

    requestNotificationPermissionIfNeeded()
    requestLocationPermissionIfNeeded()
    schedulePrayerNotifications()
  }

  private fun requestNotificationPermissionIfNeeded() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      val granted = ContextCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  private fun requestLocationPermissionIfNeeded() {
    val fineGranted = ContextCompat.checkSelfPermission(
      this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
      this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (fineGranted || coarseGranted) {
      refreshGpsLocation()
    } else {
      locationPermissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
  }

  private fun refreshGpsLocation() {
    if (isRefreshingLocation) return
    isRefreshingLocation = true
    val container = (application as MuslimApp).container
    lifecycleScope.launch {
      try {
        val gpsLocation = container.locationProvider.getCurrentLocation() ?: return@launch
        container.locationProvider.setManualLocation(gpsLocation)
        container.settingsRepository.updateSettings { settings ->
          settings.copy(
            cityName = "GPS Aktif, ${gpsLocation.cityName}",
            latitude = gpsLocation.latitude,
            longitude = gpsLocation.longitude
          )
        }
        schedulePrayerNotifications()
      } finally {
        isRefreshingLocation = false
      }
    }
  }

  private fun schedulePrayerNotifications() {
    val container = (application as MuslimApp).container
    if (!container.notificationManager.isNotificationPermissionGranted()) return
    if (!container.settingsRepository.settingsState.value.prayerNotificationEnabled) return

    lifecycleScope.launch {
      val schedule = container.prayerRepository.getTodaySchedule().first()
      schedule.prayers
        .filter { it.type in setOf(PrayerType.SUBUH, PrayerType.DZUHUR, PrayerType.ASHAR, PrayerType.MAGHRIB, PrayerType.ISYA) }
        .forEach { container.notificationManager.schedulePrayerReminder(it) }
    }
  }
}
