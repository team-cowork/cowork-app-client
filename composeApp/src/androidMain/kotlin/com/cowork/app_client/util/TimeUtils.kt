package com.cowork.app_client.util

internal actual fun nowPlusHoursIso8601(hours: Int): String =
    java.time.Instant.now().plusSeconds(hours * 3600L).toString()