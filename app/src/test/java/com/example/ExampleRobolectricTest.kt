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

  @Test
  fun `test initial destination is null at startup`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.MainViewModel(app)
    org.junit.Assert.assertNull("Destination should be null/empty on startup", viewModel.destination.value)
  }

  @Test
  fun `test favorite place room entity and custom radius`() {
    val place = com.example.data.FavoritePlace(
      name = "Gare Montparnasse",
      address = "Place Raoul-Dautry, Paris",
      latitude = 48.8412,
      longitude = 2.3204,
      tag = "Gare",
      defaultRadiusMeters = 750
    )
    assertEquals("Gare Montparnasse", place.name)
    assertEquals(750, place.defaultRadiusMeters)
    assertEquals(48.8412, place.latitude, 0.0001)
  }

  @Test
  fun `test pinning transit stop and setting as destination`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.MainViewModel(app)
    
    // Pin a transit stop at Châtelet metro station
    viewModel.pinTransitStop(48.8584, 2.3470, "Station Châtelet", "Ligne 1 / 4 / 14")
    val pinned = viewModel.pinnedStop.value
    org.junit.Assert.assertNotNull(pinned)
    assertEquals("Station Châtelet", pinned?.name)
    assertEquals(48.8584, pinned?.latitude ?: 0.0, 0.0001)

    // Set pinned stop as destination
    viewModel.setPinnedStopAsDestination()
    val dest = viewModel.destination.value
    org.junit.Assert.assertNotNull(dest)
    assertEquals("Station Châtelet", dest?.name)

    // Clear pin
    viewModel.clearPinnedStop()
    org.junit.Assert.assertNull(viewModel.pinnedStop.value)
  }

  @Test
  fun `test background service constants and favorite alert proximity logic`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Test Intent creation
    val startServiceIntent = android.content.Intent(context, com.example.service.TrackingForegroundService::class.java).apply {
      action = com.example.service.TrackingForegroundService.ACTION_START_MONITORING_FAVORITES
    }
    assertEquals(com.example.service.TrackingForegroundService.ACTION_START_MONITORING_FAVORITES, startServiceIntent.action)

    // Test favorite proximity logic
    val montparnasse = com.example.data.FavoritePlace(
      id = 101L,
      name = "Gare Montparnasse",
      address = "Paris",
      latitude = 48.8412,
      longitude = 2.3204,
      tag = "Gare",
      defaultRadiusMeters = 500
    )

    // User location 100 meters away
    val userLatNear = 48.8418
    val userLngNear = 2.3204
    val distNear = LocationTracker.calculateDistanceMeters(userLatNear, userLngNear, montparnasse.latitude, montparnasse.longitude)
    assertTrue("User should be within alert radius (< 500m)", distNear <= montparnasse.defaultRadiusMeters)

    // User location 2 km away
    val userLatFar = 48.8600
    val userLngFar = 2.3500
    val distFar = LocationTracker.calculateDistanceMeters(userLatFar, userLngFar, montparnasse.latitude, montparnasse.longitude)
    assertTrue("User should be outside alert radius (> 500m)", distFar > montparnasse.defaultRadiusMeters)
  }
}
