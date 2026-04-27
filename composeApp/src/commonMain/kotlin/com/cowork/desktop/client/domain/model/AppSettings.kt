package com.cowork.desktop.client.domain.model

enum class AppTheme(val apiValue: String, val label: String) {
    Dark("BLACK", "다크"),
    Light("WHITE", "라이트"),
}

enum class AppLanguage(val apiValue: String, val label: String) {
    Korean("KO", "한국어"),
    English("EN", "English"),
}

enum class TimeFormat(val apiValue: String, val label: String) {
    H24("24H", "24시간"),
    H12("12H", "12시간"),
}

enum class DateFormat(val apiValue: String, val label: String) {
    YYYY_MM_DD("YYYY_MM_DD", "YYYY.MM.DD"),
    MM_DD_YYYY("MM_DD_YYYY", "MM.DD.YYYY"),
    DD_MM_YYYY("DD_MM_YYYY", "DD.MM.YYYY"),
    YYYY_DD_MM("YYYY_DD_MM", "YYYY.DD.MM"),
    DD_YYYY_MM("DD_YYYY_MM", "DD.YYYY.MM"),
    MM_YYYY_DD("MM_YYYY_DD", "MM.YYYY.DD"),
}

fun String?.toAppTheme(): AppTheme = AppTheme.entries.find { it.apiValue == this } ?: AppTheme.Dark
fun String?.toAppLanguage(): AppLanguage = AppLanguage.entries.find { it.apiValue == this } ?: AppLanguage.Korean
fun String?.toTimeFormat(): TimeFormat = TimeFormat.entries.find { it.apiValue == this } ?: TimeFormat.H24
fun String?.toDateFormat(): DateFormat = DateFormat.entries.find { it.apiValue == this } ?: DateFormat.YYYY_MM_DD
