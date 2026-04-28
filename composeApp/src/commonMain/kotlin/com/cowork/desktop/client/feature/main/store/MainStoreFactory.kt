package com.cowork.desktop.client.feature.main.store

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.cowork.desktop.client.data.repository.AuthRepository
import com.cowork.desktop.client.data.repository.ChannelRepository
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.PreferenceRepository
import com.cowork.desktop.client.data.repository.SessionExpiredException
import com.cowork.desktop.client.data.repository.TeamRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.domain.model.AppLanguage
import com.cowork.desktop.client.domain.model.AppTheme
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.DateFormat
import com.cowork.desktop.client.domain.model.TeamSummary
import com.cowork.desktop.client.domain.model.TimeFormat
import com.cowork.desktop.client.domain.model.UserStatus
import com.cowork.desktop.client.domain.model.toAppLanguage
import com.cowork.desktop.client.domain.model.toAppTheme
import com.cowork.desktop.client.domain.model.toDateFormat
import com.cowork.desktop.client.domain.model.toTimeFormat
import com.cowork.desktop.client.feature.main.store.MainStore.Intent
import com.cowork.desktop.client.feature.main.store.MainStore.Label
import com.cowork.desktop.client.feature.main.store.MainStore.State
import com.cowork.desktop.client.util.parseJwtClaims
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
    private val channelRepository: ChannelRepository,
    private val chatRepository: ChatRepository,
    private val preferenceRepository: PreferenceRepository,
    private val userRepository: UserRepository,
) {
    fun create(): MainStore =
        object : MainStore, Store<Intent, State, Label> by storeFactory.create(
            name = "MainStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.Init),
            executorFactory = { Executor() },
            reducer = Reducer,
        ) {}

    private sealed interface Action {
        data object Init : Action
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Msg, Label>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.Init -> init()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Reload -> loadTeams()
                is Intent.SelectTeam -> selectTeam(intent.teamId)
                is Intent.SelectChannel -> selectChannel(intent.channelId)
                Intent.OpenCreateTeam -> dispatch(Msg.SetCreateTeamOpen(true))
                Intent.CloseCreateTeam -> dispatch(Msg.ResetCreateTeamForm)
                is Intent.ChangeCreateTeamName -> dispatch(Msg.SetCreateTeamName(intent.name))
                is Intent.ChangeCreateTeamDescription -> dispatch(Msg.SetCreateTeamDescription(intent.description))
                is Intent.SetCreateTeamIcon -> dispatch(Msg.SetCreateTeamIcon(intent.bytes, intent.contentType))
                Intent.SubmitCreateTeam -> createTeam()
                Intent.OpenCreateChannel -> dispatch(Msg.SetCreateChannelOpen(true))
                Intent.CloseCreateChannel -> dispatch(Msg.ResetCreateChannelForm)
                is Intent.ChangeCreateChannelName -> dispatch(Msg.SetCreateChannelName(intent.name))
                is Intent.ChangeCreateChannelNotice -> dispatch(Msg.SetCreateChannelNotice(intent.notice))
                is Intent.ChangeCreateChannelType -> dispatch(Msg.SetCreateChannelType(intent.type))
                Intent.SubmitCreateChannel -> createChannel()
                Intent.OpenAccountMenu -> dispatch(Msg.SetAccountMenuOpen(true))
                Intent.ToggleAccountMenu -> dispatch(Msg.SetAccountMenuOpen(!state().isAccountMenuOpen))
                Intent.CloseAccountMenu -> dispatch(Msg.SetAccountMenuOpen(false))
                Intent.OpenSettings -> dispatch(Msg.SetSettingsOpen(true))
                Intent.CloseSettings -> dispatch(Msg.SetSettingsOpen(false))
                is Intent.SetStatus -> updateStatus(intent.status, intent.expiresInHours)
                Intent.SignOut -> signOut()
                is Intent.UploadProfileImage -> uploadProfileImage(intent.bytes, intent.contentType)
                is Intent.UpdateTheme -> updateAppearance(theme = intent.theme)
                is Intent.UpdateLanguage -> updateAppearance(language = intent.language)
                is Intent.UpdateTimeFormat -> updateAppearance(timeFormat = intent.timeFormat)
                is Intent.UpdateDateFormat -> updateAppearance(dateFormat = intent.dateFormat)
                is Intent.UpdateMarketingEmail -> updateMarketingEmail(intent.enabled)
            }
        }

        // SessionExpiredException이면 강제 로그아웃 Label을 발행하고 true를 반환
        private fun Throwable.handleIfSessionExpired(): Boolean {
            if (this !is SessionExpiredException) return false
            publish(Label.SignedOut)
            return true
        }

        private fun init() {
            scope.launch {
                val tokens = authRepository.getStoredTokens()
                if (tokens != null) {
                    val claims = parseJwtClaims(tokens.accessToken)
                    dispatch(Msg.SetAccountInfo(claims.accountId, claims.email))
                    if (claims.accountId != null) {
                        val settings = preferenceRepository.getAccountSettings(claims.accountId)
                        dispatch(Msg.SetAccountStatus(
                            when (settings.status?.uppercase()) {
                                "DO_NOT_DISTURB" -> UserStatus.DoNotDisturb
                                else -> UserStatus.Online
                            }
                        ))
                        dispatch(Msg.SetAccountSettings(
                            theme = settings.theme.toAppTheme(),
                            language = settings.language.toAppLanguage(),
                            timeFormat = settings.timeFormat.toTimeFormat(),
                            dateFormat = settings.dateFormat.toDateFormat(),
                            marketingEmail = settings.marketingEmail ?: false,
                        ))
                    }
                }
            }
            scope.launch {
                val profile = userRepository.getMyProfile()
                if (profile != null) {
                    dispatch(Msg.SetUserProfile(profile))
                }
            }
            loadTeams()
            startTeamPolling()
        }

        private fun loadTeams(silent: Boolean = false) {
            scope.launch {
                if (!silent) dispatch(Msg.SetLoadingTeams(true))
                runCatching { teamRepository.getMyTeams() }
                    .onSuccess { teams ->
                        val currentSelectedTeamId = state().selectedTeamId
                        val selectedTeamId = when {
                            teams.any { it.id == currentSelectedTeamId } -> currentSelectedTeamId
                            else -> teams.firstOrNull()?.id
                        }
                        dispatch(Msg.SetTeams(teams, selectedTeamId))
                        if (selectedTeamId != null && selectedTeamId != currentSelectedTeamId) {
                            loadChannels(selectedTeamId)
                        }
                    }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        if (!silent) dispatch(Msg.SetError("팀 목록을 불러오지 못했습니다."))
                    }
                if (!silent) dispatch(Msg.SetLoadingTeams(false))
            }
        }

        private fun startTeamPolling() {
            scope.launch {
                while (true) {
                    delay(30_000)
                    loadTeams(silent = true)
                }
            }
        }

        private fun selectTeam(teamId: Long) {
            dispatch(Msg.SelectTeam(teamId))
            loadChannels(teamId)
        }

        private fun loadChannels(teamId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingChannels(true))
                runCatching { channelRepository.getTeamChannels(teamId) }
                    .onSuccess { channels ->
                        val selectedChannelId = channels.firstOrNull()?.id
                        dispatch(Msg.SetChannels(channels, selectedChannelId))
                        if (selectedChannelId != null) {
                            loadMessages(selectedChannelId)
                        }
                    }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetChannels(emptyList()))
                    }
                dispatch(Msg.SetLoadingChannels(false))
            }
        }

        private fun selectChannel(channelId: Long) {
            dispatch(Msg.SelectChannel(channelId))
            loadMessages(channelId)
        }

        private fun loadMessages(channelId: Long) {
            scope.launch {
                dispatch(Msg.SetLoadingMessages(true))
                runCatching { chatRepository.getMessages(channelId) }
                    .onSuccess { messages -> dispatch(Msg.SetMessages(messages)) }
                    .onFailure {
                        if (it.handleIfSessionExpired()) return@launch
                        dispatch(Msg.SetMessages(emptyList()))
                    }
                dispatch(Msg.SetLoadingMessages(false))
            }
        }

        private fun createTeam() {
            val name = state().createTeamName.trim()
            val description = state().createTeamDescription.trim().ifBlank { null }
            val iconBytes = state().createTeamIconBytes
            val iconContentType = state().createTeamIconContentType
            if (name.isBlank() || state().isCreatingTeam) return

            scope.launch {
                dispatch(Msg.SetCreatingTeam(true))
                runCatching {
                    val iconUrl = if (iconBytes != null && iconContentType != null) {
                        teamRepository.uploadTeamIcon(iconBytes, iconContentType)
                    } else null
                    teamRepository.createTeam(name = name, description = description, iconUrl = iconUrl)
                }.onSuccess {
                    dispatch(Msg.ResetCreateTeamForm)
                    loadTeams()
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("팀을 생성하지 못했습니다."))
                    dispatch(Msg.SetCreatingTeam(false))
                }
            }
        }

        private fun createChannel() {
            val teamId = state().selectedTeamId ?: return
            val name = state().createChannelName.trim()
            val notice = state().createChannelNotice.trim().ifBlank { null }
            val type = state().createChannelType
            if (name.isBlank() || state().isCreatingChannel) return

            scope.launch {
                dispatch(Msg.SetCreatingChannel(true))
                runCatching {
                    channelRepository.createChannel(
                        teamId = teamId,
                        type = type,
                        name = name,
                        notice = notice,
                        projectId = null,
                    )
                }.onSuccess { channel ->
                    dispatch(Msg.ResetCreateChannelForm)
                    loadChannels(channel.teamId)
                    selectChannel(channel.id)
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("채널을 생성하지 못했습니다. 서버의 cowork-channel API가 필요합니다."))
                    dispatch(Msg.SetCreatingChannel(false))
                }
            }
        }

        private fun updateStatus(status: UserStatus, expiresInHours: Double?) {
            val accountId = state().accountId ?: return
            if (state().isUpdatingStatus) return
            scope.launch {
                dispatch(Msg.SetUpdatingStatus(true))
                runCatching {
                    preferenceRepository.updateAccountStatus(accountId, status, expiresInHours)
                }.onSuccess {
                    dispatch(Msg.SetAccountStatus(status))
                    dispatch(Msg.SetAccountMenuOpen(false))
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("상태를 변경하지 못했습니다."))
                }
                dispatch(Msg.SetUpdatingStatus(false))
            }
        }

        private fun signOut() {
            scope.launch {
                authRepository.signOut()
                publish(Label.SignedOut)
            }
        }

        private fun uploadProfileImage(bytes: ByteArray, contentType: String) {
            if (state().isUploadingProfileImage) return
            scope.launch {
                dispatch(Msg.SetUploadingProfileImage(true))
                val result = runCatching { userRepository.uploadProfileImage(bytes, contentType) }
                result.onFailure { if (it.handleIfSessionExpired()) return@launch }
                val success = result.getOrDefault(false)

                if (success) {
                    val profile = runCatching { userRepository.getMyProfile() }
                        .onFailure { it.handleIfSessionExpired() }
                        .getOrNull()
                    if (profile != null) dispatch(Msg.SetUserProfile(profile))
                } else {
                    dispatch(Msg.SetError("프로필 사진을 업로드하지 못했습니다."))
                }
                dispatch(Msg.SetUploadingProfileImage(false))
            }
        }

        private fun updateAppearance(
            theme: AppTheme = state().accountTheme,
            language: AppLanguage = state().accountLanguage,
            timeFormat: TimeFormat = state().accountTimeFormat,
            dateFormat: DateFormat = state().accountDateFormat,
        ) {
            val accountId = state().accountId ?: return
            dispatch(Msg.SetAccountSettings(
                theme = theme, language = language,
                timeFormat = timeFormat, dateFormat = dateFormat,
                marketingEmail = state().accountMarketingEmail,
            ))
            scope.launch {
                runCatching {
                    preferenceRepository.updateAppearance(accountId, theme, language, timeFormat, dateFormat)
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("설정을 저장하지 못했습니다."))
                }
            }
        }

        private fun updateMarketingEmail(enabled: Boolean) {
            val accountId = state().accountId ?: return
            dispatch(Msg.SetAccountSettings(
                theme = state().accountTheme,
                language = state().accountLanguage,
                timeFormat = state().accountTimeFormat,
                dateFormat = state().accountDateFormat,
                marketingEmail = enabled,
            ))
            scope.launch {
                runCatching {
                    preferenceRepository.updateMarketingEmail(accountId, enabled)
                }.onFailure {
                    if (it.handleIfSessionExpired()) return@launch
                    dispatch(Msg.SetError("설정을 저장하지 못했습니다."))
                }
            }
        }
    }

    private sealed interface Msg {
        data class SetTeams(val teams: List<TeamSummary>, val selectedTeamId: Long?) : Msg
        data class SelectTeam(val teamId: Long) : Msg
        data class SelectChannel(val channelId: Long) : Msg
        data class SetChannels(val channels: List<Channel>, val selectedChannelId: Long? = null) : Msg
        data class SetMessages(val messages: List<ChatMessage>) : Msg
        data class SetLoadingTeams(val isLoading: Boolean) : Msg
        data class SetLoadingChannels(val isLoading: Boolean) : Msg
        data class SetLoadingMessages(val isLoading: Boolean) : Msg
        data class SetCreateTeamOpen(val isOpen: Boolean) : Msg
        data class SetCreateTeamName(val name: String) : Msg
        data class SetCreateTeamDescription(val description: String) : Msg
        data class SetCreateTeamIcon(val bytes: ByteArray, val contentType: String) : Msg
        data class SetCreatingTeam(val isCreating: Boolean) : Msg
        data class SetCreateChannelOpen(val isOpen: Boolean) : Msg
        data class SetCreateChannelName(val name: String) : Msg
        data class SetCreateChannelNotice(val notice: String) : Msg
        data class SetCreateChannelType(val type: ChannelType) : Msg
        data class SetCreatingChannel(val isCreating: Boolean) : Msg
        data class SetError(val error: String?) : Msg
        data object ResetCreateTeamForm : Msg
        data object ResetCreateChannelForm : Msg
        data class SetAccountInfo(val accountId: Long?, val email: String?) : Msg
        data class SetUserProfile(val profile: com.cowork.desktop.client.domain.model.UserProfile) : Msg
        data class SetAccountStatus(val status: UserStatus) : Msg
        data class SetAccountMenuOpen(val isOpen: Boolean) : Msg
        data class SetSettingsOpen(val isOpen: Boolean) : Msg
        data class SetUpdatingStatus(val isUpdating: Boolean) : Msg
        data class SetUploadingProfileImage(val isUploading: Boolean) : Msg
        data class SetAccountSettings(
            val theme: AppTheme,
            val language: AppLanguage,
            val timeFormat: TimeFormat,
            val dateFormat: DateFormat,
            val marketingEmail: Boolean,
        ) : Msg
        data class SetUpdatingSettings(val isUpdating: Boolean) : Msg
    }

    private object Reducer : com.arkivanov.mvikotlin.core.store.Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.SetTeams -> copy(
                teams = msg.teams,
                selectedTeamId = msg.selectedTeamId,
                error = null,
            )
            is Msg.SelectTeam -> copy(
                selectedTeamId = msg.teamId,
                channels = emptyList(),
                selectedChannelId = null,
                messages = emptyList(),
                error = null,
            )
            is Msg.SelectChannel -> copy(selectedChannelId = msg.channelId, messages = emptyList())
            is Msg.SetChannels -> copy(
                channels = msg.channels,
                selectedChannelId = msg.selectedChannelId,
                messages = if (msg.selectedChannelId == null) emptyList() else messages,
            )
            is Msg.SetMessages -> copy(messages = msg.messages)
            is Msg.SetLoadingTeams -> copy(isLoadingTeams = msg.isLoading)
            is Msg.SetLoadingChannels -> copy(isLoadingChannels = msg.isLoading)
            is Msg.SetLoadingMessages -> copy(isLoadingMessages = msg.isLoading)
            is Msg.SetCreateTeamOpen -> copy(isCreateTeamOpen = msg.isOpen, error = null)
            is Msg.SetCreateTeamName -> copy(createTeamName = msg.name)
            is Msg.SetCreateTeamDescription -> copy(createTeamDescription = msg.description)
            is Msg.SetCreateTeamIcon -> copy(createTeamIconBytes = msg.bytes, createTeamIconContentType = msg.contentType)
            is Msg.SetCreatingTeam -> copy(isCreatingTeam = msg.isCreating)
            is Msg.SetCreateChannelOpen -> copy(isCreateChannelOpen = msg.isOpen, error = null)
            is Msg.SetCreateChannelName -> copy(createChannelName = msg.name)
            is Msg.SetCreateChannelNotice -> copy(createChannelNotice = msg.notice)
            is Msg.SetCreateChannelType -> copy(createChannelType = msg.type)
            is Msg.SetCreatingChannel -> copy(isCreatingChannel = msg.isCreating)
            is Msg.SetError -> copy(error = msg.error)
            Msg.ResetCreateTeamForm -> copy(
                isCreateTeamOpen = false,
                createTeamName = "",
                createTeamDescription = "",
                createTeamIconBytes = null,
                createTeamIconContentType = null,
                isCreatingTeam = false,
            )
            Msg.ResetCreateChannelForm -> copy(
                isCreateChannelOpen = false,
                createChannelName = "",
                createChannelNotice = "",
                createChannelType = ChannelType.Text,
                isCreatingChannel = false,
            )
            is Msg.SetAccountInfo -> copy(accountId = msg.accountId, accountEmail = msg.email)
            is Msg.SetUserProfile -> copy(
                accountName = msg.profile.name,
                accountEmail = msg.profile.email.ifBlank { accountEmail },
                accountNickname = msg.profile.nickname,
                accountProfileImageUrl = msg.profile.profileImageUrl,
                accountGithub = msg.profile.github,
                accountStudentNumber = msg.profile.studentNumber,
                accountMajor = msg.profile.major,
                accountStudentRole = msg.profile.studentRole,
                accountDescription = msg.profile.description,
                accountRoles = msg.profile.roles,
            )
            is Msg.SetAccountStatus -> copy(accountStatus = msg.status)
            is Msg.SetAccountMenuOpen -> copy(isAccountMenuOpen = msg.isOpen)
            is Msg.SetSettingsOpen -> copy(isSettingsOpen = msg.isOpen, isAccountMenuOpen = false)
            is Msg.SetUpdatingStatus -> copy(isUpdatingStatus = msg.isUpdating)
            is Msg.SetUploadingProfileImage -> copy(isUploadingProfileImage = msg.isUploading)
            is Msg.SetAccountSettings -> copy(
                accountTheme = msg.theme,
                accountLanguage = msg.language,
                accountTimeFormat = msg.timeFormat,
                accountDateFormat = msg.dateFormat,
                accountMarketingEmail = msg.marketingEmail,
            )
            is Msg.SetUpdatingSettings -> copy(isUpdatingSettings = msg.isUpdating)
        }
    }
}
