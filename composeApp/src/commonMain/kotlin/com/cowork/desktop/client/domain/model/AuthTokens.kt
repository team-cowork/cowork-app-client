package com.cowork.desktop.client.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)
