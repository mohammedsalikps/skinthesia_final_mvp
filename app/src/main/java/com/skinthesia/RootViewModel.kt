package com.skinthesia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.navigation.MainRoutes
import com.skinthesia.core.navigation.OnboardingRoutes
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take

/**
 * Decides where the app opens. Resolved once from persisted state so the
 * splash screen can hold until the answer is known and the NavHost start
 * destination never changes underneath the user.
 */
class RootViewModel(
    repository: UserProfileRepository,
) : ViewModel() {

    val startDestination: StateFlow<String?> = repository.profile
        .map { profile -> if (profile.onboardingCompleted) MainRoutes.ROOT else OnboardingRoutes.GRAPH }
        .take(1)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
}
