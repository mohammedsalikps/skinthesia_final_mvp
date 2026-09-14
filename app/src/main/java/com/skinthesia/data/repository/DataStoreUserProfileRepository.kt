package com.skinthesia.data.repository

import com.skinthesia.data.local.UserProfileLocalDataSource
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

/** DataStore-backed [UserProfileRepository]. */
class DataStoreUserProfileRepository(
    private val local: UserProfileLocalDataSource,
) : UserProfileRepository {

    override val profile: Flow<UserProfile> = local.profile

    override suspend fun updateSkinProfile(transform: (SkinProfile) -> SkinProfile) {
        local.update { it.copy(skinProfile = transform(it.skinProfile)) }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        local.update { it.copy(onboardingCompleted = completed) }
    }

    override suspend fun setDisplayName(name: String?) {
        local.update { it.copy(displayName = name?.trim()?.ifBlank { null }) }
    }
}
