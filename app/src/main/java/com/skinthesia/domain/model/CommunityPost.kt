package com.skinthesia.domain.model

data class CommunityAuthor(
    val id: String,
    val displayName: String,
    val avatar: ImageSource? = null,
    val isExpert: Boolean = false,
    /** For experts, e.g. "Dermatologist". */
    val credential: String? = null,
)

data class CommunityComment(
    val id: String,
    val author: CommunityAuthor,
    val text: String,
    val createdAt: Long,
    val likes: Int = 0,
)

/** A post in the Skinthesia community feed (screens 19 and 20). */
data class CommunityPost(
    val id: String,
    val author: CommunityAuthor,
    val text: String,
    val images: List<ImageSource> = emptyList(),
    val likes: Int = 0,
    val comments: List<CommunityComment> = emptyList(),
    val createdAt: Long,
    val topic: String? = null,
    val likedByMe: Boolean = false,
)
