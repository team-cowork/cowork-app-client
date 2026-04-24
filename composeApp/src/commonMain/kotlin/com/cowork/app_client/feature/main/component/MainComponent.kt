package com.cowork.app_client.feature.main.component

import com.cowork.app_client.data.local.LayoutPreferenceStorage
import com.cowork.app_client.domain.model.AppLanguage
import com.cowork.app_client.domain.model.AppTheme
import com.cowork.app_client.domain.model.ChannelType
import com.cowork.app_client.domain.model.DateFormat
import com.cowork.app_client.domain.model.TimeFormat
import com.cowork.app_client.domain.model.UserStatus
import com.cowork.app_client.feature.main.store.MainStore
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {
    val state: StateFlow<MainStore.State>
    val layoutPreferenceStorage: LayoutPreferenceStorage

    fun onTeamClick(teamId: Long)
    fun onChannelClick(channelId: Long)
    fun onCreateTeamClick()
    fun onCreateTeamDismiss()
    fun onCreateTeamNameChange(name: String)
    fun onCreateTeamDescriptionChange(description: String)
    fun onCreateTeamIconChange(bytes: ByteArray, contentType: String)
    fun onCreateTeamSubmit()
    fun onCreateChannelClick()
    fun onCreateChannelDismiss()
    fun onCreateChannelNameChange(name: String)
    fun onCreateChannelNoticeChange(notice: String)
    fun onCreateChannelTypeChange(type: ChannelType)
    fun onCreateChannelSubmit()
    fun onAccountMenuClick()
    fun onAccountMenuDismiss()
    fun onSettingsClick()
    fun onSettingsDismiss()
    fun onStatusChange(status: UserStatus, expiresInHours: Double?)
    fun onSignOutClick()
    fun onUploadProfileImage(bytes: ByteArray, contentType: String)
    fun onReloadClick()
    fun onThemeChange(theme: AppTheme)
    fun onLanguageChange(language: AppLanguage)
    fun onTimeFormatChange(timeFormat: TimeFormat)
    fun onDateFormatChange(dateFormat: DateFormat)
    fun onMarketingEmailChange(enabled: Boolean)
}
