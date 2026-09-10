package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.sensor.CompassSensorManager
import com.example.domain.model.TasbihState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Muslim Mas An", appName)
  }

  @Test
  fun `calculate qibla bearing for Jakarta coordinates`() {
    // Jakarta approx latitude -6.2088, longitude 106.8456
    val bearing = CompassSensorManager.calculateQiblaBearing(-6.2088, 106.8456)
    // Qibla from Jakarta is approximately 295 degrees (West-Northwest)
    assertTrue("Bearing should be between 290 and 300 degrees", bearing in 290f..300f)
  }

  @Test
  fun `tasbih state test`() {
    val tasbih = TasbihState(count = 32, target = 33)
    assertEquals(32, tasbih.count)
    assertEquals(33, tasbih.target)
  }
}
