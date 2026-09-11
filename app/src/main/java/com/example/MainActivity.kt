package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.AppThemeSetting
import com.example.domain.model.PrayerType
import com.example.ui.localization.localizedLayoutDirection
import com.example.ui.navigation.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private var isRefreshingLocation by mutableStateOf(false)
  private var showLocationDisabledDialog by mutableStateOf(false)

  private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) refreshGpsLocation()
  }

  private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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
        CompositionLocalProvider(LocalLayoutDirection provides localizedLayoutDirection(settings.appLanguage)) {
          MainAppScaffold(
            container = container,
            onRefreshLocation = { requestLocationPermissionIfNeeded() },
            isRefreshingLocation = isRefreshingLocation
          )
          if (showLocationDisabledDialog) {
            AlertDialog(
              onDismissRequest = { showLocationDisabledDialog = false },
              title = { Text("Lokasi/GPS belum aktif") },
              text = { Text("Aktifkan layanan lokasi di pengaturan HP agar Teman Sholat dapat menemukan lokasi Anda secara otomatis.") },
              confirmButton = { Button(onClick = { showLocationDisabledDialog = false; openLocationSettings() }) { Text("Nyalakan GPS") } },
              dismissButton = { Button(onClick = { showLocationDisabledDialog = false }) { Text("Batal") } }
            )
          }
        }
      }
    }
    requestNotificationPermissionIfNeeded()
    requestLocationPermissionIfNeeded()
    schedulePrayerNotifications()
    lifecycleScope.launch {
      container.settingsRepository.settingsState.collect {
        schedulePrayerNotifications()
      }
    }
  }

  private fun isLocationEnabled(): Boolean {
    val manager = getSystemService(LOCATION_SERVICE) as LocationManager
    return try {
      manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } catch (_: Exception) { false }
  }

  private fun openLocationSettings() { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }

  private fun requestNotificationPermissionIfNeeded() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
      if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  private fun requestLocationPermissionIfNeeded() {
    val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (fineGranted || coarseGranted) {
      if (isLocationEnabled()) refreshGpsLocation() else showLocationDisabledDialog = true
    } else {
      locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  private fun refreshGpsLocation() {
    if (isRefreshingLocation) return
    if (!isLocationEnabled()) { showLocationDisabledDialog = true; return }
    isRefreshingLocation = true
    val container = (application as MuslimApp).container
    lifecycleScope.launch {
      try {
        val gpsLocation = container.locationProvider.getCurrentLocation() ?: return@launch
        showLocationDisabledDialog = false
        container.locationProvider.setManualLocation(gpsLocation)
        container.settingsRepository.updateSettings { settings -> settings.copy(cityName = gpsLocation.cityName, latitude = gpsLocation.latitude, longitude = gpsLocation.longitude, elevationMeters = gpsLocation.elevationMeters) }
        schedulePrayerNotifications()
      } finally { isRefreshingLocation = false }
    }
  }

  override fun onResume() {
    super.onResume()
    val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (fineGranted || coarseGranted) { if (isLocationEnabled()) refreshGpsLocation() else showLocationDisabledDialog = true }
  }

  private fun schedulePrayerNotifications() {
    val container = (application as MuslimApp).container
    if (!container.notificationManager.isNotificationPermissionGranted()) return
    val settings = container.settingsRepository.settingsState.value
    if (!settings.prayerNotificationEnabled) { container.notificationManager.cancelAllReminders(); return }
    lifecycleScope.launch {
      val currentSettings = container.settingsRepository.settingsState.value
      container.notificationManager.cancelAllReminders()
      val schedule = container.prayerRepository.getTodaySchedule().first()
      schedule.prayers.filter { prayer -> when (prayer.type) {
        PrayerType.SUBUH -> currentSettings.subuhNotificationEnabled
        PrayerType.DZUHUR -> currentSettings.dzuhurNotificationEnabled
        PrayerType.ASHAR -> currentSettings.asharNotificationEnabled
        PrayerType.MAGHRIB -> currentSettings.maghribNotificationEnabled
        PrayerType.ISYA -> currentSettings.isyaNotificationEnabled
        else -> false
      } }.forEach { prayer -> container.notificationManager.schedulePrayerReminder(prayer, currentSettings.notificationSound) }
    }
  }
}
