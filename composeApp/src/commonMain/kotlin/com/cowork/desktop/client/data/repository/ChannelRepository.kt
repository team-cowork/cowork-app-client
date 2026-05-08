package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ChannelApi
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelType

interface ChannelRepository {
    suspend fun getTeamChannels(teamId: Long): List<Channel>
    suspend fun getChannel(channelId: Long): Channel
    suspend fun createChannel(
        teamId: Long,
        type: ChannelType,
        name: String,
        description: String?,
        isPrivate: Boolean,
    ): Channel
    suspend fun updateChannel(
        channelId: Long,
        name: String? = null,
        description: String? = null,
        isPrivate: Boolean? = null,
    ): Channel
    suspend fun deleteChannel(channelId: Long)
    suspend fun getMembers(channelId: Long): List<ChannelApi.ChannelMemberResponse>
    suspend fun addMember(channelId: Long, userId: Long): ChannelApi.ChannelMemberResponse
    suspend fun removeMember(channelId: Long, memberId: Long)
}
