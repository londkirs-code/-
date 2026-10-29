package com.example

import com.example.alarm.VatikinAlarmManager
import com.example.calculator.AstronomicalCalculator
import com.example.model.PredefinedLocations
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class VatikinAlarmTest {

    @Test
    fun testAlarmCalculatesExactMinutesBeforeNetz() {
        val location = PredefinedLocations.DEFAULT
        val minutesBefore = 45

        val nextAlarm = VatikinAlarmManager.calculateNextAlarmDateTime(
            location = location,
            minutesBeforeNetz = minutesBefore,
            isPlainNetz = false
        )

        // The alarm must be in the future
        val now = ZonedDateTime.now(ZoneId.of(location.timeZoneId))
        assertTrue("Alarm must be after current moment", nextAlarm.isAfter(now))

        // Calculate tomorrow's netz to verify difference
        val alarmDate = nextAlarm.toLocalDate()
        val zmanim = AstronomicalCalculator.calculateZmanim(alarmDate, location)
        val expectedNetz = zmanim.netzVisible

        val expectedAlarmTime = expectedNetz.minusMinutes(minutesBefore.toLong())
        assertEquals(
            "Alarm must equal netz minus specified minutes",
            expectedAlarmTime.toEpochSecond(),
            nextAlarm.toEpochSecond()
        )
    }

    @Test
    fun testDifferentMinutesBeforeNetz() {
        val location = PredefinedLocations.DEFAULT
        val minutes30 = 30
        val minutes60 = 60

        val alarm30 = VatikinAlarmManager.calculateNextAlarmDateTime(location, minutes30, false)
        val alarm60 = VatikinAlarmManager.calculateNextAlarmDateTime(location, minutes60, false)

        val diffMinutes = java.time.Duration.between(alarm60, alarm30).toMinutes()
        assertEquals(30L, diffMinutes)
    }
}
