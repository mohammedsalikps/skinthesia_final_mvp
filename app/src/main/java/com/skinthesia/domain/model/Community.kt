package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CommunitySection(val label: String, val description: String) {
    FOR_YOU("For You", "Picked around your goals"),
    QUESTIONS("Questions", "Ask and answer"),
    DISCUSSIONS("Discussions", "Routines, products and ideas"),
    SUCCESS_STORIES("Success Stories", "Progress, in people's own words"),
    EXPERT_CORNER("Expert Corner", "Notes from our specialists"),
    ;

    companion object {
        /** Sections a member can post into. */
        val postable: List<CommunitySection> = listOf(QUESTIONS, DISCUSSIONS, SUCCESS_STORIES)
    }
}

@Serializable
data class CommunityAuthor(
    val id: String,
    val displayName: String,
    val isExpert: Boolean = false,
    val credential: String? = null,
    val journeyWeek: Int? = null,
    val isCurrentUser: Boolean = false,
)

@Serializable
data class CommunityComment(
    val id: String,
    val postId: String,
    val author: CommunityAuthor,
    val body: String,
    val createdAt: Long,
    val likeCount: Int,
    val likedByMe: Boolean = false,
    val isExpertAnswer: Boolean = false,
)

/** A community post. Text-first by design: the community is about learning, not comparing faces. */
@Serializable
data class CommunityPost(
    val id: String,
    val section: CommunitySection,
    val author: CommunityAuthor,
    val title: String,
    val body: String,
    val tags: List<String>,
    val createdAt: Long,
    val likeCount: Int,
    val likedByMe: Boolean = false,
    val comments: List<CommunityComment> = emptyList(),
    val relatedGoals: Set<SkinGoal> = emptySet(),
    val isPinned: Boolean = false,
) {
    val hasExpertAnswer: Boolean get() = comments.any { it.isExpertAnswer }
}
