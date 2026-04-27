package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelType

interface ChannelRepository {
    suspend fun getTeamChannels(teamId: Long): List<Channel>
    suspend fun createChannel(
        teamId: Long,
        type: ChannelType,
        name: String,
        notice: String?,
        projectId: Long?,
    ): Channel
}
