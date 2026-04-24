package com.cowork.app_client.data.repository

import com.cowork.app_client.data.remote.PreferenceApi
import com.cowork.app_client.domain.model.AppLanguage
import com.cowork.app_client.domain.model.AppTheme
import com.cowork.app_client.domain.model.DateFormat
import com.cowork.app_client.domain.model.TimeFormat
import com.cowork.app_client.domain.model.UserStatus
import com.cowork.app_client.domain.model.toAppLanguage
import com.cowork.app_client.domain.model.toAppTheme
import com.cowork.app_client.domain.model.toDateFormat
import com.cowork.app_client.domain.model.toTimeFormat
import com.cowork.app_client.util.nowPlusHoursIso8601

interface PreferenceRepository {
    suspend fun getAccountStatus(accountId: Long): UserStatus
    suspend fun getAccountSettings(accountId: Long): PreferenceApi.AccountSettings
    suspend fun updateAccountStatus(accountId: Long, status: UserStatus, expiresInHours: Double?)
    suspend fun updateAppearance(accountId: Long, theme: AppTheme, language: AppLanguage, timeFormat: TimeFormat, dateFormat: DateFormat)
    suspend fun updateMarketingEmail(accountId: Long, enabled: Boolean)
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

    override suspend fun getAccountSettings(accountId: Long): PreferenceApi.AccountSettings =
        authRepository.authorized { token ->
            preferenceApi.getAccountSettings(token, accountId)
        }

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
                request = PreferenceApi.UpdateAccountSettingsRequest(
                    status = status.toApiValue(),
                    statusExpiresAt = expiresAt,
                ),
            )
        }
    }

    override suspend fun updateAppearance(
        accountId: Long,
        theme: AppTheme,
        language: AppLanguage,
        timeFormat: TimeFormat,
        dateFormat: DateFormat,
    ) {
        authRepository.authorized { token ->
            preferenceApi.updateAccountSettings(
                accessToken = token,
                accountId = accountId,
                request = PreferenceApi.UpdateAccountSettingsRequest(
                    theme = theme.apiValue,
                    language = language.apiValue,
                    timeFormat = timeFormat.apiValue,
                    dateFormat = dateFormat.apiValue,
                ),
            )
        }
    }

    override suspend fun updateMarketingEmail(accountId: Long, enabled: Boolean) {
        authRepository.authorized { token ->
            preferenceApi.updateAccountSettings(
                accessToken = token,
                accountId = accountId,
                request = PreferenceApi.UpdateAccountSettingsRequest(marketingEmail = enabled),
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
