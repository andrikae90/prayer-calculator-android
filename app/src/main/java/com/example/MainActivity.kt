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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.AppThemeSetting
import com.example.ui.navigation.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) refreshGpsLocation()
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
          onRefreshLocation = { requestLocationPermissionIfNeeded() }
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
      locationPermissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
  }

  private fun refreshGpsLocation() {
    val container = (application as MuslimApp).container
    lifecycleScope.launch {
      val gpsLocation = container.locationProvider.getCurrentLocation() ?: return@launch
      container.locationProvider.setManualLocation(gpsLocation)
      container.settingsRepository.updateSettings { settings ->
        settings.copy(
          // The home location chip uses the text before the first comma.
          // Prefix it so the user can immediately see that GPS is active.
          cityName = "GPS Aktif, ${gpsLocation.cityName}",
          latitude = gpsLocation.latitude,
          longitude = gpsLocation.longitude
        )
      }
    }
  }
}
