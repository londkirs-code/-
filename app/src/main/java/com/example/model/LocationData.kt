package com.example.model

data class LocationData(
    val id: String,
    val nameHebrew: String,
    val nameEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double,
    val timeZoneId: String = "Asia/Jerusalem",
    val visibleNetzOffsetMinutes: Int = 0 // minutes relative to plain sea-level netz
)

object PredefinedLocations {
    val CITIES = listOf(
        LocationData("jerusalem", "ירושלים", "Jerusalem", 31.7683, 35.2137, 754.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 5),
        LocationData("bnei_brak", "בני ברק", "Bnei Brak", 32.0834, 34.8344, 35.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 0),
        LocationData("tel_aviv", "תל אביב - יפו", "Tel Aviv", 32.0853, 34.7818, 15.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 0),
        LocationData("haifa", "חיפה", "Haifa", 32.7940, 34.9896, 120.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 1),
        LocationData("tzfat", "צפת", "Tzfat", 32.9646, 35.4960, 840.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 3),
        LocationData("beer_sheva", "באר שבע", "Beer Sheva", 31.2529, 34.7915, 260.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 0),
        LocationData("ashdod", "אשדוד", "Ashdod", 31.8044, 34.6553, 20.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 0),
        LocationData("petah_tikva", "פתח תקווה", "Petah Tikva", 32.0840, 34.8878, 45.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 0),
        LocationData("netanya", "נתניה", "Netanya", 32.3215, 34.8532, 25.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 0),
        LocationData("beit_shemesh", "בית שמש", "Beit Shemesh", 31.7470, 34.9881, 220.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 2),
        LocationData("modiin", "מודיעין עילית", "Modi'in Illit", 31.9324, 35.0440, 310.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 3),
        LocationData("tiberias", "טבריה", "Tiberias", 32.7922, 35.5312, -200.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 11),
        LocationData("eilat", "אילת", "Eilat", 29.5577, 34.9519, 12.0, "Asia/Jerusalem", visibleNetzOffsetMinutes = 0),
        LocationData("new_york", "ניו יורק (ברוקלין)", "New York (Brooklyn)", 40.6782, -73.9442, 10.0, "America/New_York", visibleNetzOffsetMinutes = 0),
        LocationData("lakewood", "לייקווד", "Lakewood, NJ", 40.0979, -74.2177, 15.0, "America/New_York", visibleNetzOffsetMinutes = 0),
        LocationData("london", "לונדון", "London", 51.5074, -0.1278, 25.0, "Europe/London", visibleNetzOffsetMinutes = 0),
        LocationData("paris", "פריז", "Paris", 48.8566, 2.3522, 35.0, "Europe/Paris", visibleNetzOffsetMinutes = 0)
    )

    val DEFAULT = CITIES.first() // Jerusalem
}
