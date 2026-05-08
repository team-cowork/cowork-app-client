package com.cowork.desktop.client.domain.model

data class Thread(
    val id: Long,
    val channelId: Long,
    val name: String,
    val parentMessageId: String,
    val isArchived: Boolean,
    val createdBy: Long,
)
