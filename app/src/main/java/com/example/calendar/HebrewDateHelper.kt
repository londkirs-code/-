package com.example.calendar

import java.time.DayOfWeek
import java.time.LocalDate

data class HebrewDate(
    val day: Int,
    val month: Int, // 1 = Tishrei, 2 = Cheshvan, ..., 7 = Adar I, etc.
    val year: Int,
    val isLeapYear: Boolean
) {
    fun formatHebrew(includeDayOfWeek: Boolean = true, dayOfWeek: DayOfWeek? = null): String {
        val dayStr = toHebrewNumeral(day)
        val monthStr = getHebrewMonthName(month, isLeapYear)
        val yearStr = toHebrewYearNumeral(year)
        val prefix = if (includeDayOfWeek && dayOfWeek != null) {
            "${getDayOfWeekHebrew(dayOfWeek)}, "
        } else ""

        return "$prefix$dayStr ב$monthStr $yearStr"
    }

    companion object {
        fun toHebrewNumeral(number: Int): String {
            if (number <= 0) return number.toString()
            val letters = listOf(
                400 to "ת", 300 to "ש", 200 to "ר", 100 to "ק",
                90 to "צ", 80 to "פ", 70 to "ע", 60 to "ס", 50 to "נ", 40 to "מ", 30 to "ל", 20 to "כ",
                10 to "י", 9 to "ט", 8 to "ח", 7 to "ז", 6 to "ו", 5 to "ה", 4 to "ד", 3 to "ג", 2 to "ב", 1 to "א"
            )
            var n = number
            val sb = StringBuilder()
            // Special cases in Hebrew numbers: 15 is טו (9+6), 16 is טז (9+7)
            if (n == 15) return "ט״ו"
            if (n == 16) return "ט״ז"

            for ((value, letter) in letters) {
                while (n >= value) {
                    sb.append(letter)
                    n -= value
                }
            }
            val res = sb.toString()
            return when (res.length) {
                0 -> number.toString()
                1 -> "$res׳"
                else -> "${res.substring(0, res.length - 1)}״${res.last()}"
            }
        }

        fun toHebrewYearNumeral(year: Int): String {
            // e.g. 5787 -> תשפ״ז (5000 is omitted or designated by ה׳)
            val remainder = year % 1000
            val numStr = toHebrewNumeral(remainder)
            return "ה'$numStr"
        }

        fun getHebrewMonthName(month: Int, isLeap: Boolean): String {
            return if (!isLeap) {
                when (month) {
                    1 -> "תשרי"
                    2 -> "חשוון"
                    3 -> "כסלו"
                    4 -> "טבת"
                    5 -> "שבט"
                    6 -> "אדר"
                    7 -> "ניסן"
                    8 -> "אייר"
                    9 -> "סיוון"
                    10 -> "תמוז"
                    11 -> "אב"
                    12 -> "אלול"
                    else -> ""
                }
            } else {
                when (month) {
                    1 -> "תשרי"
                    2 -> "חשוון"
                    3 -> "כסלו"
                    4 -> "טבת"
                    5 -> "שבט"
                    6 -> "אדר א׳"
                    7 -> "אדר ב׳"
                    8 -> "ניסן"
                    9 -> "אייר"
                    10 -> "סיוון"
                    11 -> "תמוז"
                    12 -> "אב"
                    13 -> "אלול"
                    else -> ""
                }
            }
        }

        fun getDayOfWeekHebrew(dayOfWeek: DayOfWeek): String {
            return when (dayOfWeek) {
                DayOfWeek.SUNDAY -> "יום ראשון"
                DayOfWeek.MONDAY -> "יום שני"
                DayOfWeek.TUESDAY -> "יום שלישי"
                DayOfWeek.WEDNESDAY -> "יום רביעי"
                DayOfWeek.THURSDAY -> "יום חמישי"
                DayOfWeek.FRIDAY -> "יום שישי"
                DayOfWeek.SATURDAY -> "שבת קודש"
            }
        }
    }
}

object HebrewDateHelper {

    fun isHebrewLeapYear(year: Int): Boolean {
        return (year * 7 + 1) % 19 < 7
    }

    private fun hebrewCalendarElapsedDays(year: Int): Long {
        val y = (year - 1).toLong()
        val monthsElapsed = (235L * (y / 19L)) + (12L * (y % 19L)) + ((((y % 19L) * 7L) + 1L) / 19L)
        val partsElapsed = 204L + (793L * (monthsElapsed % 1080L))
        val hoursElapsed = 5L + (12L * monthsElapsed) + (793L * (monthsElapsed / 1080L)) + (partsElapsed / 1080L)
        val partsRemainder = partsElapsed % 1080L
        var day = 1L + (29L * monthsElapsed) + (hoursElapsed / 24L)
        val dayOfWeek = (day % 7L).toInt()
        val hour = (hoursElapsed % 24L).toInt()

        // Postponements (דחיית ראש השנה)
        if (dayOfWeek == 2 || dayOfWeek == 4 || dayOfWeek == 6) { // לא אד״ו ראש (Sun=1, Mon=2, Wed=4, Fri=6)
            day += 1L
        } else if (dayOfWeek == 1 && hour >= 18) { // מולד זקן ביום ראשון
            day += 2L
        } else if (dayOfWeek == 0 && hour >= 9 && (hour > 9 || partsRemainder >= 204L) && !isHebrewLeapYear(year)) {
            day += 2L
        } else if (dayOfWeek == 6 && hour >= 15 && (hour > 15 || partsRemainder >= 589L) && isHebrewLeapYear(year - 1)) {
            day += 1L
        }
        return day
    }

    private fun daysInHebrewYear(year: Int): Int {
        return (hebrewCalendarElapsedDays(year + 1) - hebrewCalendarElapsedDays(year)).toInt()
    }

    fun fromLocalDate(localDate: LocalDate): HebrewDate {
        // Epoch reference: Jan 1, 1970 is Hebrew 23 Tevet 5730
        // Julian Day Number algorithm for conversion
        var hYear = localDate.year + 3760
        var elapsed = hebrewCalendarElapsedDays(hYear)
        val absDate = localDate.toEpochDay() + 719163L + 1373429L // Julian Day to Hebrew Epoch alignment

        // Adjust Hebrew year
        while (hebrewCalendarElapsedDays(hYear + 1) <= absDate) {
            hYear++
        }
        while (hebrewCalendarElapsedDays(hYear) > absDate) {
            hYear--
        }

        var dayInYear = (absDate - hebrewCalendarElapsedDays(hYear) + 1).toInt()
        val isLeap = isHebrewLeapYear(hYear)
        val yearDays = daysInHebrewYear(hYear)
        val isCheshvanFull = (yearDays % 10 == 5)
        val isKislevShort = (yearDays % 10 == 3)

        val monthDays = if (!isLeap) {
            intArrayOf(
                30, // Tishrei
                if (isCheshvanFull) 30 else 29, // Cheshvan
                if (isKislevShort) 29 else 30, // Kislev
                29, // Tevet
                30, // Shevat
                29, // Adar
                30, // Nisan
                29, // Iyar
                30, // Sivan
                29, // Tammuz
                30, // Av
                29  // Elul
            )
        } else {
            intArrayOf(
                30, // Tishrei
                if (isCheshvanFull) 30 else 29, // Cheshvan
                if (isKislevShort) 29 else 30, // Kislev
                29, // Tevet
                30, // Shevat
                30, // Adar I
                29, // Adar II
                30, // Nisan
                29, // Iyar
                30, // Sivan
                29, // Tammuz
                30, // Av
                29  // Elul
            )
        }

        var monthIndex = 0
        while (monthIndex < monthDays.size && dayInYear > monthDays[monthIndex]) {
            dayInYear -= monthDays[monthIndex]
            monthIndex++
        }

        return HebrewDate(
            day = dayInYear.coerceAtLeast(1),
            month = monthIndex + 1,
            year = hYear,
            isLeapYear = isLeap
        )
    }
}
