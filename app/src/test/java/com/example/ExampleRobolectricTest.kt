package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.AstronomicalCalculator
import com.example.calendar.HebrewDateHelper
import com.example.model.PredefinedLocations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("נץ החמה", appName)
    }

    @Test
    fun `astronomical calculator calculates valid netz and zmanim`() {
        val date = LocalDate.of(2026, 10, 21)
        val jerusalem = PredefinedLocations.DEFAULT
        val zmanim = AstronomicalCalculator.calculateZmanim(date, jerusalem)

        assertNotNull(zmanim.netzPlain)
        assertNotNull(zmanim.netzVisible)
        assertNotNull(zmanim.shkia)
        // Netz must be before Shkia
        assertTrue(zmanim.netzPlain.isBefore(zmanim.shkia))
        // Alot HaShachar must be before Netz
        assertTrue(zmanim.alotHaShachar16Degrees.isBefore(zmanim.netzPlain))
        // Sof Zman Shema Gra must be between Netz and Shkia
        assertTrue(zmanim.sofZmanShemaGra.isAfter(zmanim.netzPlain))
        assertTrue(zmanim.sofZmanShemaGra.isBefore(zmanim.shkia))
    }

    @Test
    fun `hebrew date helper converts gregorian to hebrew date`() {
        val date = LocalDate.of(2026, 10, 21)
        val hebrewDate = HebrewDateHelper.fromLocalDate(date)

        assertTrue(hebrewDate.year >= 5780)
        assertTrue(hebrewDate.month in 1..13)
        assertTrue(hebrewDate.day in 1..30)
        val formatted = hebrewDate.formatHebrew()
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun `compass manager initializes and provides state`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = com.example.compass.CompassSensorManager(context)
        val state = manager.compassState.value
        assertNotNull(state)
        // Tel Aviv to Jerusalem is East-South-East (~115-125 degrees)
        manager.updateCoordinates(32.0853, 34.7818) // Tel Aviv
        val updatedState = manager.compassState.value
        assertTrue(updatedState.bearingToJerusalemDegrees in 100f..140f)
        assertTrue(updatedState.distanceToJerusalemKm in 40..75)
    }
}
