package com.cowork.desktop.client.domain.model

data class Channel(
    val id: Long,
    val teamId: Long,
    val projectId: Long?,
    val type: ChannelType,
    val name: String,
    val notice: String?,
    val isArchived: Boolean,
)

enum class ChannelType {
    Text,
    Voice,
    Webhook,
    MeetingNote,
    Unknown,
}
