package com.skinthesia.data.repository

import com.skinthesia.data.local.JsonPreferenceStore
import com.skinthesia.domain.model.AppSettings
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

/** DataStore-backed profile. A cloud profile service can replace it behind the interface. */
class DataStoreUserProfileRepository(
    private val store: JsonPreferenceStore<UserProfile>,
    private val clock: () -> Long = System::currentTimeMillis,
) : UserProfileRepository {

    override val profile: Flow<UserProfile> = store.data

    override suspend fun current(): UserProfile = store.current()

    override suspend fun update(transform: (UserProfile) -> UserProfile): UserProfile =
        store.update { current ->
            val next = transform(current)
            if (next.id.isBlank()) next.copy(id = Ids.new("user"), createdAt = clock()) else next
        }

    override suspend fun clear() = store.clear()
}

class DataStoreSettingsRepository(
    private val store: JsonPreferenceStore<AppSettings>,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = store.data
    override suspend fun current(): AppSettings = store.current()
    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.update(transform)
    }
    override suspend fun clear() = store.clear()
}
