package com.cowork.app_client.data.repository

import com.cowork.app_client.domain.model.AuthTokens
import com.cowork.app_client.feature.auth.OAuthAuthorizationCode
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

interface AuthRepository {
    suspend fun getStoredTokens(): AuthTokens?
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun exchangeAuthorizationCode(authorizationCode: OAuthAuthorizationCode): AuthTokens
    // 실패 시 null 반환. 동시 호출은 내부에서 직렬화된다.
    suspend fun refreshTokens(): AuthTokens?
    suspend fun signOut()
    fun getSignInUrl(): String
}

class SessionExpiredException : Exception("세션이 만료되었습니다. 다시 로그인해주세요.")

/**
 * 401 응답 시 토큰을 갱신하고 한 번 재시도한다.
 * refresh 자체가 실패하면 [SessionExpiredException]을 던진다.
 */
suspend fun <T> AuthRepository.authorized(block: suspend (accessToken: String) -> T): T {
    val tokens = getStoredTokens() ?: throw SessionExpiredException()
    return try {
        block(tokens.accessToken)
    } catch (e: ClientRequestException) {
        if (e.response.status != HttpStatusCode.Unauthorized) throw e
        val refreshed = refreshTokens() ?: throw SessionExpiredException()
        block(refreshed.accessToken)
    }
}
