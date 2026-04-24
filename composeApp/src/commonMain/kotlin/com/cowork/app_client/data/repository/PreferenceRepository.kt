package com.cowork.app_client.data.repository

import com.cowork.app_client.data.remote.PreferenceApi
import com.cowork.app_client.domain.model.UserStatus
import com.cowork.app_client.util.nowPlusHoursIso8601

interface PreferenceRepository {
    suspend fun getAccountStatus(accountId: Long): UserStatus
    suspend fun updateAccountStatus(accountId: Long, status: UserStatus, expiresInHours: Double?)
}

class DefaultPreferenceRepository(
    private val authRepository: AuthRepository,
    private val preferenceApi: PreferenceApi,
) : PreferenceRepository {

    override suspend fun getAccountStatus(accountId: Long): UserStatus =
        runCatching {
            authRepository.authorized { token ->
                preferenceApi.getAccountSettings(token, accountId).status.toUserStatus()
            }
        }.getOrDefault(UserStatus.Online)

    override suspend fun updateAccountStatus(accountId: Long, status: UserStatus, expiresInHours: Double?) {
        val expiresAt = if (status == UserStatus.DoNotDisturb && expiresInHours != null) {
            nowPlusHoursIso8601(expiresInHours)
        } else {
            null
        }
        authRepository.authorized { token ->
            preferenceApi.updateAccountSettings(
                accessToken = token,
                accountId = accountId,
                status = status.toApiValue(),
                statusExpiresAt = expiresAt,
            )
        }
    }

    private fun String?.toUserStatus(): UserStatus = when (this?.uppercase()) {
        "DO_NOT_DISTURB" -> UserStatus.DoNotDisturb
        else -> UserStatus.Online
    }

    private fun UserStatus.toApiValue(): String = when (this) {
        UserStatus.Online -> "ONLINE"
        UserStatus.DoNotDisturb -> "DO_NOT_DISTURB"
    }
}
