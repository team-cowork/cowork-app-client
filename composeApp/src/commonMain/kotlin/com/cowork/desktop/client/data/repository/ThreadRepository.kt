package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.Thread

interface ThreadRepository {
    suspend fun getThreads(channelId: Long, includeArchived: Boolean = false): List<Thread>
    suspend fun createThread(channelId: Long, name: String, parentMessageId: String): Thread
    suspend fun updateThread(channelId: Long, threadId: Long, name: String? = null, isArchived: Boolean? = null): Thread
}
