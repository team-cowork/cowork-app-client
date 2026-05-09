package com.cowork.desktop.client.domain.model

data class Webhook(
    val id: Long,
    val channelId: Long,
    val name: String,
    val isSecure: Boolean,
    val token: String?,
    val avatarUrl: String?,
    val createdBy: Long,
)
