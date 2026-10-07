package com.example.model

enum class AlarmTone(val id: String, val labelFr: String, val labelAr: String, val labelEn: String) {
    SIREN("siren", "Sirène forte", "صفارة قوية", "Loud Siren"),
    URGENT("urgent", "Bips urgents", "تنبيه سريع", "Urgent Beeps"),
    SOFT("soft", "Carillon doux", "نغمة هادئة", "Gentle Chime"),
    RADAR("radar", "Radar sonar", "سونار الرادار", "Sonar Radar"),
    BELL("bell", "Cloche de train", "جرس القطار", "Transit Bell")
}

enum class MapStyle(val id: String, val labelFr: String, val labelAr: String, val labelEn: String) {
    OPENSTREETMAP("osm", "OpenStreetMap", "خريطة الشارع المفتوحة", "OpenStreetMap"),
    GOOGLE_MAPS("google", "Google Maps (Voyager)", "خرائط جوجل", "Google Maps"),
    SATELLITE("satellite", "Satellite (Esri / Google)", "قمر صناعي", "Satellite"),
    TERRAIN("terrain", "Relief (OpenTopo)", "تضاريس", "Terrain"),
    DARK("dark", "Sombre Carto", "داكن", "Carto Dark")
}

enum class AppLanguage(val code: String, val label: String, val isRtl: Boolean) {
    FR("fr", "Français", false),
    AR("ar", "العربية", true),
    EN("en", "English", false)
}

data class LocationPoint(
    val name: String,
    val address: String = "",
    val latitude: Double,
    val longitude: Double
)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val speedKmh: Float = 0f,
    val isGpsActive: Boolean = false,
    val isSimulated: Boolean = false
)

data class TripState(
    val isActive: Boolean = false,
    val destination: LocationPoint? = null,
    val alertRadiusMeters: Int = 500,
    val currentDistanceMeters: Float = Float.MAX_VALUE,
    val estimatedArrivalSeconds: Int = 0,
    val isWithinAlertZone: Boolean = false,
    val isAlarmRinging: Boolean = false,
    val isAlarmMuted: Boolean = false,
    val initialDistanceMeters: Float = 0f
)
