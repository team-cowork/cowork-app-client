package com.cowork.desktop.client.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.cowork.desktop.client.feature.auth.component.AuthComponent
import com.cowork.desktop.client.feature.main.component.MainComponent

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Auth(val component: AuthComponent) : Child
        data class Main(val component: MainComponent) : Child
    }
}
