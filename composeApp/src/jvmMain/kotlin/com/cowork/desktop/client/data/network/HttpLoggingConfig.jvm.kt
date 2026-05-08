package com.cowork.desktop.client.data.network

actual fun isHttpLoggingEnabled(): Boolean =
    System.getProperty("cowork.httpLogging")?.equals("true", ignoreCase = true) == true
