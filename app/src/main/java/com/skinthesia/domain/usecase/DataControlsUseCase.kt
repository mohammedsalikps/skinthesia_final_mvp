package com.skinthesia.domain.usecase

import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.CartRepository
import com.skinthesia.domain.repository.CommunityRepository
import com.skinthesia.domain.repository.ConsultationRepository
import com.skinthesia.domain.repository.OrderRepository
import com.skinthesia.domain.repository.PhotoStore
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository

/** Privacy controls: delete photos only, or erase everything and start fresh. */
class DataControlsUseCase(
    private val profiles: UserProfileRepository,
    private val settings: SettingsRepository,
    private val assessments: AssessmentRepository,
    private val probes: SkinProbeRepository,
    private val plans: PlanRepository,
    private val cart: CartRepository,
    private val orders: OrderRepository,
    private val consultations: ConsultationRepository,
    private val community: CommunityRepository,
    private val photos: PhotoStore,
) {
    /** Deletes every selfie and removes photo references from history; scores are kept. */
    suspend fun deletePhotos() {
        assessments.completedList().forEach { assessment ->
            if (assessment.photo != null) assessments.update(assessment.id) { it.copy(photo = null) }
        }
        profiles.current().activeAssessmentId?.let { id -> assessments.update(id) { it.copy(photo = null) } }
        photos.deleteAll()
    }

    /** Erases all local data, returning the app to its first-run state. */
    suspend fun deleteEverything() {
        photos.deleteAll()
        assessments.deleteAll()
        probes.deleteAll()
        plans.deleteAll()
        cart.clear()
        orders.deleteAll()
        consultations.deleteAll()
        community.deleteUserContent()
        settings.clear()
        profiles.clear()
    }
}
