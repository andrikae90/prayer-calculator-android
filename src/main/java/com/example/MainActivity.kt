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
import com.example.ui.navigation.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private var isRefreshingLocation by mutableStateOf(false)
  private var locationStatus by mutableStateOf("Lokasi Manual")

  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      refreshGpsLocation()
    } else {
      locationStatus = "Izin Lokasi Ditolak"
    }
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
          isRefreshingLocation = isRefreshingLocation,
          locationStatus = locationStatus
        )
      }
    }

    requestLocationPermissionIfNeeded()
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
      locationStatus = "Meminta Izin Lokasi..."
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
    locationStatus = "Memperbarui Lokasi..."
    val container = (application as MuslimApp).container
    lifecycleScope.launch {
      try {
        val gpsLocation = container.locationProvider.getCurrentLocation()
        if (gpsLocation == null) {
          locationStatus = "GPS Tidak Tersedia"
          return@launch
        }

        container.locationProvider.setManualLocation(gpsLocation)
        container.settingsRepository.updateSettings { settings ->
          settings.copy(
            cityName = "GPS Aktif, ${gpsLocation.cityName}",
            latitude = gpsLocation.latitude,
            longitude = gpsLocation.longitude
          )
        }
        locationStatus = "GPS Aktif"
      } finally {
        isRefreshingLocation = false
      }
    }
  }
}
