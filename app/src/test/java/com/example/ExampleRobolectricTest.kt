package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AlarmTone
import com.example.model.LocationPoint
import com.example.service.LocationTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ARRIVA", appName)
  }

  @Test
  fun `test distance calculation`() {
    // Paris Gare de Lyon to Châtelet (approx 2.5 - 3 km)
    val dist = LocationTracker.calculateDistanceMeters(
      48.8448, 2.3735,
      48.8614, 2.3470
    )
    assertTrue("Distance between Gare de Lyon and Châtelet should be approx 2-3km", dist in 2000f..3500f)
  }

  @Test
  fun `test alarm tone properties`() {
    assertEquals("siren", AlarmTone.SIREN.id)
    assertEquals("urgent", AlarmTone.URGENT.id)
    assertEquals("soft", AlarmTone.SOFT.id)
  }
}
