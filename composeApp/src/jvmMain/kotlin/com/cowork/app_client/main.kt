package com.cowork.app_client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.cowork.app_client.data.repository.AuthRepository
import com.cowork.app_client.data.repository.ChannelRepository
import com.cowork.app_client.data.repository.ChatRepository
import com.cowork.app_client.data.repository.TeamRepository
import com.cowork.app_client.di.commonModule
import com.cowork.app_client.di.jvmModule
import com.cowork.app_client.feature.auth.OAuthLauncher
import com.cowork.app_client.navigation.DefaultRootComponent
import org.koin.core.context.startKoin
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JFrame

fun main() {
    val isMacOs = System.getProperty("os.name")
        .lowercase()
        .contains("mac")

    val koin = startKoin {
        modules(commonModule, jvmModule)
    }.koin

    val lifecycle = LifecycleRegistry()
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
        storeFactory = DefaultStoreFactory(),
        authRepository = koin.get<AuthRepository>(),
        teamRepository = koin.get<TeamRepository>(),
        channelRepository = koin.get<ChannelRepository>(),
        chatRepository = koin.get<ChatRepository>(),
        oAuthLauncher = koin.get<OAuthLauncher>(),
    )

    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "cowork",
            state = windowState,
            undecorated = !isMacOs,
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(960, 620)
                if (isMacOs) {
                    (window as? JFrame)?.rootPane?.apply {
                        putClientProperty("apple.awt.fullWindowContent", true)
                        putClientProperty("apple.awt.transparentTitleBar", true)
                        putClientProperty("apple.awt.windowTitleVisible", false)
                    }
                }
            }

            if (isMacOs) {
                App(root)
            } else {
                DesktopWindowFrame(
                    onClose = ::exitApplication,
                ) {
                    App(root)
                }
            }
        }
    }
}

@Composable
private fun WindowScope.DesktopWindowFrame(
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(Color(0xFF1E1F22))
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        val frame = window as? Frame
                        if (frame?.extendedState == Frame.MAXIMIZED_BOTH) return@detectDragGestures

                        val location = window.location
                        window.setLocation(
                            (location.x + dragAmount.x).toInt(),
                            (location.y + dragAmount.y).toInt(),
                        )
                    }
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF1E1F22)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "cowork",
                    color = Color(0xFFE3E5E8),
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.weight(1f))
                WindowControlButton(label = "−") {
                    (window as? Frame)?.extendedState = Frame.ICONIFIED
                }
                WindowControlButton(label = "□") {
                    val frame = window as? Frame ?: return@WindowControlButton
                    val isMaximized = frame.extendedState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH
                    frame.extendedState = if (isMaximized) Frame.NORMAL else Frame.MAXIMIZED_BOTH
                }
                WindowControlButton(
                    label = "×",
                    background = Color(0xFFED4245),
                    onClick = onClose,
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun WindowControlButton(
    label: String,
    background: Color = Color.Transparent,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 34.dp)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color(0xFFE3E5E8),
        )
    }
}
