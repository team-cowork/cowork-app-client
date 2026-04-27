package com.cowork.desktop.client.feature.auth.component

import com.cowork.desktop.client.feature.auth.store.AuthStore
import kotlinx.coroutines.flow.StateFlow

interface AuthComponent {
    val state: StateFlow<AuthStore.State>
    fun onLoginClick()
}
