package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Tournament
import com.example.ui.components.formatCountdown
import com.example.ui.state.ArenaViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FF Arena", appName)
    }

    @Test
    fun `default placement points follow Free Fire BR scoring`() {
        assertEquals(12L, ArenaViewModel.defaultPlacementPoints(1))
        assertEquals(9L, ArenaViewModel.defaultPlacementPoints(2))
        assertEquals(1L, ArenaViewModel.defaultPlacementPoints(10))
        assertEquals(0L, ArenaViewModel.defaultPlacementPoints(12))
    }

    @Test
    fun `tournament available slots never drops below zero`() {
        val t = Tournament(maxSlots = 12L, registeredSlots = 14L)
        assertEquals(0L, t.availableSlots)
    }

    @Test
    fun `countdown formats open now when target passed`() {
        assertEquals("OPEN NOW", formatCountdown(1000L, 2000L))
        assertEquals("01:00:00", formatCountdown(3_601_000L, 1000L))
    }
}
