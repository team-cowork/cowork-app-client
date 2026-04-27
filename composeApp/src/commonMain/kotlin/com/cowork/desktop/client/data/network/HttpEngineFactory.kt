package com.cowork.desktop.client.data.network

import io.ktor.client.engine.HttpClientEngine

expect fun createHttpEngine(): HttpClientEngine
