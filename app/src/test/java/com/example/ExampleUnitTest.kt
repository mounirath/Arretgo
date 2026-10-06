package com.example

import com.example.model.AlarmTone
import com.example.model.AppLanguage
import com.example.service.LocationTracker
import com.example.ui.components.formatDistance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDistanceCalculation() {
    val dist = LocationTracker.calculateDistanceMeters(
      48.8448, 2.3735,
      48.8614, 2.3470
    )
    assertTrue("Distance between Gare de Lyon and Chatelet should be approx 2-3km", dist in 2000f..3500f)
  }

  @Test
  fun testDistanceFormatting() {
    assertEquals("500 m", formatDistance(500))
    assertEquals("1 km", formatDistance(1000))
    assertEquals("2.5 km", formatDistance(2500))
  }

  @Test
  fun testLanguages() {
    assertEquals("fr", AppLanguage.FR.code)
    assertEquals("ar", AppLanguage.AR.code)
    assertTrue(AppLanguage.AR.isRtl)
  }

  @Test
  fun testAlarmTones() {
    assertEquals("siren", AlarmTone.SIREN.id)
    assertEquals("urgent", AlarmTone.URGENT.id)
    assertEquals("soft", AlarmTone.SOFT.id)
  }
}
