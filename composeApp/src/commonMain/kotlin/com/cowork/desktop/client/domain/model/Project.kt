package com.cowork.desktop.client.domain.model

data class Project(
    val id: Long,
    val teamId: Long,
    val name: String,
    val description: String?,
    val status: ProjectStatus,
    val createdBy: Long,
)

enum class ProjectStatus {
    Active,
    Archived,
    Unknown,
}

data class ProjectMember(
    val id: Long,
    val projectId: Long,
    val userId: Long,
    val role: ProjectRole,
)

enum class ProjectRole {
    Owner,
    Editor,
    Viewer,
    Unknown,
}
