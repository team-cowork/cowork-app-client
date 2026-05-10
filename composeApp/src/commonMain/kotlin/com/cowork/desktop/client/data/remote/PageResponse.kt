package com.cowork.desktop.client.data.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 0,
)
