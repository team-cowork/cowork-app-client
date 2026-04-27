package com.cowork.desktop.client

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform