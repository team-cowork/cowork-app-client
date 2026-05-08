package com.cowork.desktop.client.domain.model

data class Channel(
    val id: Long,
    val teamId: Long,
    val name: String,
    val type: ChannelType,
    val description: String?,
    val isPrivate: Boolean,
)

enum class ChannelType {
    Text,
    Voice,
    Webhook,
    MeetingNote,
    AccountShare,
    FileShare,
    Unknown,
}
