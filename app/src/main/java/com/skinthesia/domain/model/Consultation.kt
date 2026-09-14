package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConsultationType(val label: String, val description: String, val durationMinutes: Int) {
    VIDEO("Video consultation", "A face-to-face conversation on video", 30),
    CHAT("Chat consultation", "Message back and forth with your expert", 20),
    SKINPRINT_REVIEW("SkinPrint review", "Your expert reviews your report and replies in writing", 15),
}

@Serializable
data class ExpertReview(
    val author: String,
    val rating: Int,
    val text: String,
)

@Serializable
data class ConsultationExpert(
    val id: String,
    val name: String,
    /** For example "Consultant Dermatologist". */
    val title: String,
    val credentials: String,
    val specialties: List<String>,
    val focusGoals: Set<SkinGoal>,
    val yearsExperience: Int,
    val rating: Float,
    val reviewCount: Int,
    val languages: List<String>,
    val bio: String,
    val approach: String,
    val consultationTypes: Set<ConsultationType>,
    /** Fee in whole rupees for a standard consultation. */
    val fee: Int,
    val reviews: List<ExpertReview>,
)

@Serializable
data class ConsultationSlot(
    val id: String,
    val expertId: String,
    val startAt: Long,
    val durationMinutes: Int,
    val isAvailable: Boolean,
)

@Serializable
enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
}

/** A consultation booking. Scheduling is simulated locally until a real calendar is connected. */
@Serializable
data class Booking(
    val id: String,
    val expertId: String,
    val expertName: String,
    val startAt: Long,
    val durationMinutes: Int,
    val type: ConsultationType,
    val notes: String,
    val shareSkinPrint: Boolean,
    val createdAt: Long,
    val status: BookingStatus,
    val isSimulated: Boolean = true,
)
