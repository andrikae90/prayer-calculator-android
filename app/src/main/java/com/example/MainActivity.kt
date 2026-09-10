package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.domain.model.AppThemeSetting
import com.example.ui.navigation.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
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
        MainAppScaffold(container = container)
      }
    }
  }
}

