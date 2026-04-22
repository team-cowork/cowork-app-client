package com.cowork.app_client.domain.model

data class UserProfile(
    val id: Long,
    val name: String,
    val email: String,
    val nickname: String?,
    val profileImageUrl: String?,
    val github: String?,
    val studentRole: String?,
    val studentNumber: String?,
    val major: String?,
    val specialty: String?,
    val description: String?,
    val roles: List<String>,
)
