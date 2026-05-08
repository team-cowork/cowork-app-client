package com.cowork.desktop.client.util

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.ByteArrayInputStream
internal actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? =
    runCatching { ByteArrayInputStream(bytes).readAllBytes().decodeToImageBitmap() }.getOrNull()
