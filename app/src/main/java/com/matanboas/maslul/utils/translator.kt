package com.matanboas.maslul.utils

// Translation maps
val monthTranslations = mapOf(
    "January" to "ינואר",
    "February" to "פברואר",
    "March" to "מרץ",
    "April" to "אפריל",
    "May" to "מאי",
    "June" to "יוני",
    "July" to "יולי",
    "August" to "אוגוסט",
    "September" to "ספטמבר",
    "October" to "אוקטובר",
    "November" to "נובמבר",
    "December" to "דצמבר"
)

val fieldTranslations = mapOf(
    // Difficulty levels
    "Easy" to "קל",
    "Moderate" to "בינוני",
    "Challenging" to "מאתגר",
    // Access types
    "Foot" to "רגלית",
    "Bicycles" to "אופניים",
    "All Vehicles" to "כלי רכב",
    "Off-Road Vehicles" to "רכבי שטח",
    // Entrance fee
    "Free" to "חינם",
    "Paid" to "בתשלום",
    // Trail types
    "Short Linear" to "קווי קצר",
    "Circular" to "מעגלי",
    "Long Linear" to "קווי ארוך",
    // Nature reserve
    "Yes" to "כן",
    "No" to "לא"
)

// Function to translate seasons
fun translateSeasons(seasons: String): String {
    var translated = seasons
    monthTranslations.forEach { (en, he) ->
        translated = translated.replace(en, he, ignoreCase = true)
    }
    return translated
}

// Function to translate text
fun translateText(text: String): String {
    return fieldTranslations[text] ?: text
}