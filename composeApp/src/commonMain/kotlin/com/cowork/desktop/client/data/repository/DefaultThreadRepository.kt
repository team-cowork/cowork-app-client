package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ThreadApi
import com.cowork.desktop.client.domain.model.Thread

class DefaultThreadRepository(
    private val authRepository: AuthRepository,
    private val threadApi: ThreadApi,
) : ThreadRepository {

    override suspend fun getThreads(channelId: Long, includeArchived: Boolean): List<Thread> =
        authorized { threadApi.getThreads(it, channelId, includeArchived) }

    override suspend fun createThread(channelId: Long, name: String, parentMessageId: String): Thread =
        authorized { threadApi.createThread(it, channelId, name, parentMessageId) }

    override suspend fun updateThread(
        channelId: Long,
        threadId: Long,
        name: String?,
        isArchived: Boolean?,
    ): Thread =
        authorized { threadApi.updateThread(it, channelId, threadId, name, isArchived) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
