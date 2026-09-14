package com.skinthesia.domain.repository

import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Source of truth for the local user and their skin profile.
 *
 * Phase 1 persists to DataStore; a remote implementation can replace it
 * without touching feature code.
 */
interface UserProfileRepository {
    /** Emits the current profile and every subsequent change. */
    val profile: Flow<UserProfile>

    /** Applies [transform] atomically to the stored skin profile. */
    suspend fun updateSkinProfile(transform: (SkinProfile) -> SkinProfile)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setDisplayName(name: String?)
}
