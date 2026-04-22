package com.cowork.app_client.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMyProfile(accessToken: String): MyProfileResponse =
        client.get("$baseUrl/users/me") {
            bearerAuth(accessToken)
        }.body<ApiResponse<MyProfileResponse>>().data ?: MyProfileResponse()

    @Serializable
    data class MyProfileResponse(
        val id: Long? = null,
        val name: String? = null,
        val email: String? = null,
        val sex: String? = null,
        val github: String? = null,
        val accountDescription: String? = null,
        val studentRole: String? = null,
        val studentNumber: String? = null,
        val major: String? = null,
        val specialty: String? = null,
        val status: String? = null,
        val nickname: String? = null,
        val roles: List<String> = emptyList(),
        val description: String? = null,
        @SerialName("profileImageUrl")
        val profileImageUrl: String? = null,
    )
}
