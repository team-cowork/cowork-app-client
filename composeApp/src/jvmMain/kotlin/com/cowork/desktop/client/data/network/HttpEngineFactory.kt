package com.cowork.desktop.client.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun createHttpEngine(): HttpClientEngine = CIO.create()
