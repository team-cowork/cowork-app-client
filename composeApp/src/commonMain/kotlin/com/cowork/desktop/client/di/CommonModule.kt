package com.cowork.desktop.client.di

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.cowork.desktop.client.config.AppConfig
import com.cowork.desktop.client.data.network.createHttpClient
import com.cowork.desktop.client.data.network.createHttpEngine
import com.cowork.desktop.client.data.network.ConnectivityMonitor
import com.cowork.desktop.client.data.remote.AuthApi
import com.cowork.desktop.client.data.remote.ChannelApi
import com.cowork.desktop.client.data.remote.ChatApi
import com.cowork.desktop.client.data.remote.PreferenceApi
import com.cowork.desktop.client.data.remote.TeamApi
import com.cowork.desktop.client.data.remote.UserApi
import com.cowork.desktop.client.data.repository.AuthRepository
import com.cowork.desktop.client.data.repository.ChannelRepository
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.DefaultChannelRepository
import com.cowork.desktop.client.data.repository.DefaultAuthRepository
import com.cowork.desktop.client.data.repository.DefaultChatRepository
import com.cowork.desktop.client.data.repository.DefaultPreferenceRepository
import com.cowork.desktop.client.data.repository.DefaultTeamRepository
import com.cowork.desktop.client.data.repository.DefaultUserRepository
import com.cowork.desktop.client.data.repository.PreferenceRepository
import com.cowork.desktop.client.data.repository.TeamRepository
import com.cowork.desktop.client.data.repository.UserRepository
import org.koin.dsl.module

val commonModule = module {
    single { DefaultStoreFactory() }
    single { createHttpEngine() }
    single { createHttpClient(get()) }
    single<ConnectivityMonitor> { ConnectivityMonitor(httpClient = get(), healthUrl = "${AppConfig.COWORK_API_BASE_URL}/health") }
    single { AuthApi(client = get(), baseUrl = AppConfig.COWORK_API_BASE_URL) }
    single { TeamApi(client = get(), baseUrl = AppConfig.COWORK_API_BASE_URL) }
    single { ChannelApi(client = get(), baseUrl = AppConfig.COWORK_API_BASE_URL) }
    single { ChatApi(client = get(), baseUrl = AppConfig.COWORK_API_BASE_URL) }
    single { PreferenceApi(client = get(), baseUrl = AppConfig.COWORK_API_BASE_URL) }
    single { UserApi(client = get(), baseUrl = AppConfig.COWORK_API_BASE_URL) }
    single<AuthRepository> { DefaultAuthRepository(tokenStorage = get(), authApi = get()) }
    single<TeamRepository> { DefaultTeamRepository(authRepository = get(), teamApi = get()) }
    single<ChannelRepository> { DefaultChannelRepository(authRepository = get(), channelApi = get()) }
    single<ChatRepository> { DefaultChatRepository(authRepository = get(), chatApi = get()) }
    single<PreferenceRepository> { DefaultPreferenceRepository(authRepository = get(), preferenceApi = get()) }
    single<UserRepository> { DefaultUserRepository(authRepository = get(), userApi = get()) }
}
