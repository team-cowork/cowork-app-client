package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.WebhookApi
import com.cowork.desktop.client.domain.model.Webhook

interface WebhookRepository {
    suspend fun getWebhooks(channelId: Long): List<Webhook>
    suspend fun createWebhook(channelId: Long, name: String, avatarUrl: String?, isSecure: Boolean): Webhook
    suspend fun updateWebhook(channelId: Long, webhookId: Long, name: String?, avatarUrl: String?, isSecure: Boolean?): Webhook
    suspend fun deleteWebhook(channelId: Long, webhookId: Long)
}

class DefaultWebhookRepository(
    private val authRepository: AuthRepository,
    private val webhookApi: WebhookApi,
) : WebhookRepository {

    override suspend fun getWebhooks(channelId: Long): List<Webhook> =
        authRepository.authorized { webhookApi.getWebhooks(it, channelId) }

    override suspend fun createWebhook(channelId: Long, name: String, avatarUrl: String?, isSecure: Boolean): Webhook =
        authRepository.authorized { webhookApi.createWebhook(it, channelId, name, avatarUrl, isSecure) }

    override suspend fun updateWebhook(channelId: Long, webhookId: Long, name: String?, avatarUrl: String?, isSecure: Boolean?): Webhook =
        authRepository.authorized { webhookApi.updateWebhook(it, channelId, webhookId, name, avatarUrl, isSecure) }

    override suspend fun deleteWebhook(channelId: Long, webhookId: Long) =
        authRepository.authorized { webhookApi.deleteWebhook(it, channelId, webhookId) }
}
