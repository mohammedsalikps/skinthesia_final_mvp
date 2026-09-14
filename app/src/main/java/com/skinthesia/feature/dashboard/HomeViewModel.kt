package com.skinthesia.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Exposes the live user profile to the Home tab. */
class HomeViewModel(
    repository: UserProfileRepository,
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = repository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
