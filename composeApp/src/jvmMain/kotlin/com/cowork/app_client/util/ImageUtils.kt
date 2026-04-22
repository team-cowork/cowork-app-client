package com.cowork.app_client.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import java.io.ByteArrayInputStream

internal actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? =
    runCatching { loadImageBitmap(ByteArrayInputStream(bytes)) }.getOrNull()
