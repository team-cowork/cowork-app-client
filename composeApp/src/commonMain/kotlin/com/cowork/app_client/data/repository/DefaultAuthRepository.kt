package com.cowork.app_client.data.repository

import com.cowork.app_client.data.local.TokenStorage
import com.cowork.app_client.data.remote.AuthApi
import com.cowork.app_client.domain.model.AuthTokens
import com.cowork.app_client.feature.auth.OAuthAuthorizationCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultAuthRepository(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
) : AuthRepository {

    private val refreshMutex = Mutex()

    override suspend fun getStoredTokens(): AuthTokens? {
        val access = tokenStorage.getAccessToken() ?: return null
        val refresh = tokenStorage.getRefreshToken() ?: return null
        return AuthTokens(access, refresh)
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
    }

    override suspend fun exchangeAuthorizationCode(authorizationCode: OAuthAuthorizationCode): AuthTokens =
        authApi.exchangeAuthorizationCode(authorizationCode)

    override suspend fun refreshTokens(): AuthTokens? = refreshMutex.withLock {
        // 뮤텍스 대기 중 다른 코루틴이 이미 갱신했을 수 있으므로 저장된 토큰을 재확인
        val refreshToken = tokenStorage.getRefreshToken() ?: return@withLock null
        runCatching {
            val tokens = authApi.refresh(refreshToken)
            tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
            tokens
        }.getOrElse {
            // refresh 실패 시 만료된 토큰을 지워 재시도 방지
            tokenStorage.clearTokens()
            null
        }
    }

    override suspend fun signOut() {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()
        if (accessToken != null && refreshToken != null) {
            runCatching { authApi.signOut(accessToken, refreshToken) }
        }
        tokenStorage.clearTokens()
    }

    override fun getSignInUrl(): String = authApi.getSignInUrl()
}
