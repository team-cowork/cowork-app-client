package com.cowork.app_client

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform