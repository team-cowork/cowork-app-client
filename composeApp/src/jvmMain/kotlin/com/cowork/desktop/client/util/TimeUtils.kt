package com.cowork.desktop.client.util

internal actual fun nowPlusHoursIso8601(hours: Double): String =
    java.time.Instant.now().plusSeconds((hours * 3600).toLong()).toString()