package com.cowork.app_client.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal data class JwtClaims(
    val accountId: Long? = null,
    val email: String? = null,
)

@OptIn(ExperimentalEncodingApi::class)
internal fun parseJwtClaims(jwt: String): JwtClaims {
    val rawPayload = jwt.split(".").getOrNull(1) ?: return JwtClaims()
    return try {
        val padLength = (4 - rawPayload.length % 4) % 4
        val padded = rawPayload + "=".repeat(padLength)
        val decoded = Base64.UrlSafe.decode(padded)
        val json = decoded.decodeToString()
        val subRegex = Regex(""""sub"\s*:\s*"?(\d+)"?""")
        val emailRegex = Regex(""""email"\s*:\s*"([^"]+)"""")
        JwtClaims(
            accountId = subRegex.find(json)?.groupValues?.getOrNull(1)?.toLongOrNull(),
            email = emailRegex.find(json)?.groupValues?.getOrNull(1),
        )
    } catch (_: Exception) {
        JwtClaims()
    }
}