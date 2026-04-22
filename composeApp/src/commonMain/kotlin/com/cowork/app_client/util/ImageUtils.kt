package com.cowork.app_client.util

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap?
