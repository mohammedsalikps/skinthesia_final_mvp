package com.skinthesia.domain.model

/** The local user. Identity fields are ready for a future account backend. */
data class UserProfile(
    val id: String,
    val displayName: String? = null,
    val createdAt: Long,
    val onboardingCompleted: Boolean = false,
    val skinProfile: SkinProfile = SkinProfile.Empty,
)
