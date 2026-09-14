package com.skinthesia.domain.usecase

import com.skinthesia.domain.repository.UserProfileRepository

/** Marks onboarding as finished so the app opens on the main shell next time. */
class CompleteOnboardingUseCase(
    private val repository: UserProfileRepository,
) {
    suspend operator fun invoke() {
        repository.setOnboardingCompleted(true)
    }
}
