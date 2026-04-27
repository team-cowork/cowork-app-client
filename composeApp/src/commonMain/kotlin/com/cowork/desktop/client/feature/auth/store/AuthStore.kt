package com.cowork.desktop.client.feature.auth.store

import com.arkivanov.mvikotlin.core.store.Store
import com.cowork.desktop.client.feature.auth.store.AuthStore.Intent
import com.cowork.desktop.client.feature.auth.store.AuthStore.Label
import com.cowork.desktop.client.feature.auth.store.AuthStore.State

interface AuthStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object Login : Intent
        data object SignOut : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Label {
        data object Authenticated : Label
        data object SignedOut : Label
    }
}
