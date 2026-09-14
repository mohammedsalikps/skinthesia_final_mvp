package com.skinthesia.feature.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinthesia.data.local.PhotoStore
import com.skinthesia.domain.model.AgeRange
import com.skinthesia.domain.model.LensFacing
import com.skinthesia.domain.model.LifestyleFactor
import com.skinthesia.domain.model.PhotoSource
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPhoto
import com.skinthesia.domain.model.SkinTarget
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.TargetGoal
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.BuildTargetPhraseUseCase
import com.skinthesia.domain.usecase.CompleteOnboardingUseCase
import com.skinthesia.domain.usecase.SuggestTargetGoalsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/** Transient, self-dismissing messages surfaced on onboarding screens. */
enum class OnboardingNotice { GOAL_LIMIT, CAPTURE_FAILED, IMPORT_FAILED }

/** One-shot navigation events. */
sealed interface OnboardingEvent {
    data object Completed : OnboardingEvent
}

/** Everything the six onboarding screens render. */
data class OnboardingUiState(
    val isLoaded: Boolean = false,
    val photo: SkinPhoto? = null,
    val selectedGoals: List<SkinGoal> = emptyList(),
    val targetGoals: Set<TargetGoal> = emptySet(),
    val targetPhrase: String = "",
    val ageRange: AgeRange? = null,
    val skinType: SkinType? = null,
    val concerns: Set<SkinConcern> = emptySet(),
    val lifestyleFactors: Set<LifestyleFactor> = emptySet(),
    val isCapturing: Boolean = false,
    val isImporting: Boolean = false,
    val isSaving: Boolean = false,
    val notice: OnboardingNotice? = null,
) {
    val goalLimitReached: Boolean get() = selectedGoals.size >= SkinGoal.MAX_SELECTION
    val isBusyWithPhoto: Boolean get() = isCapturing || isImporting
    val canContinueFromPhoto: Boolean get() = photo != null && !isBusyWithPhoto
    val canContinueFromGoals: Boolean get() = selectedGoals.isNotEmpty()
    val canContinueFromTarget: Boolean get() = targetGoals.isNotEmpty()
    val canContinueFromQuestionnaire: Boolean get() = ageRange != null && skinType != null
}

/**
 * Shared across the onboarding graph. Every answer is persisted as it changes,
 * so leaving and returning resumes exactly where the user stopped.
 */
class OnboardingViewModel(
    private val repository: UserProfileRepository,
    private val photoStore: PhotoStore,
    private val buildTargetPhrase: BuildTargetPhraseUseCase,
    private val suggestTargetGoals: SuggestTargetGoalsUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events: Flow<OnboardingEvent> = _events.receiveAsFlow()

    /** Once the user edits targets by hand we stop re-deriving them from goals. */
    private var targetCustomized = false

    init {
        viewModelScope.launch {
            val skin = repository.profile.first().skinProfile
            targetCustomized = skin.target != null
            val targets = skin.target?.goals?.takeIf { it.isNotEmpty() } ?: suggestTargetGoals(skin.goals)
            _uiState.update {
                it.copy(
                    isLoaded = true,
                    photo = skin.baselinePhoto,
                    selectedGoals = skin.goals,
                    targetGoals = targets,
                    targetPhrase = buildTargetPhrase(skin.goals, targets),
                    ageRange = skin.ageRange,
                    skinType = skin.skinType,
                    concerns = skin.concerns,
                    lifestyleFactors = skin.lifestyleFactors,
                )
            }
        }
    }

    // ---- 02 Take Your Photo -------------------------------------------------

    /**
     * Runs [takePicture] against a fresh file from the photo store and records
     * the result. The camera layer supplies the actual capture call.
     */
    fun capturePhoto(lensFacing: LensFacing, takePicture: suspend (File) -> Unit) {
        if (_uiState.value.isBusyWithPhoto) return
        _uiState.update { it.copy(isCapturing = true, notice = null) }
        viewModelScope.launch {
            val target = photoStore.newCaptureFile()
            val result = runCatching { takePicture(target) }
            if (result.isSuccess && target.exists()) {
                val photo = SkinPhoto(
                    id = UUID.randomUUID().toString(),
                    filePath = target.absolutePath,
                    capturedAt = System.currentTimeMillis(),
                    source = PhotoSource.CAMERA,
                    lensFacing = lensFacing,
                    mirrored = lensFacing == LensFacing.FRONT,
                )
                replacePhoto(photo)
                _uiState.update { it.copy(isCapturing = false) }
            } else {
                target.delete()
                _uiState.update { it.copy(isCapturing = false, notice = OnboardingNotice.CAPTURE_FAILED) }
            }
        }
    }

    fun importPhoto(uri: Uri) {
        if (_uiState.value.isBusyWithPhoto) return
        _uiState.update { it.copy(isImporting = true, notice = null) }
        viewModelScope.launch {
            val result = runCatching { photoStore.importFromUri(uri) }
            val file = result.getOrNull()
            if (file != null) {
                val photo = SkinPhoto(
                    id = UUID.randomUUID().toString(),
                    filePath = file.absolutePath,
                    capturedAt = System.currentTimeMillis(),
                    source = PhotoSource.GALLERY,
                )
                replacePhoto(photo)
                _uiState.update { it.copy(isImporting = false) }
            } else {
                _uiState.update { it.copy(isImporting = false, notice = OnboardingNotice.IMPORT_FAILED) }
            }
        }
    }

    fun retakePhoto() {
        val previous = _uiState.value.photo ?: return
        _uiState.update { it.copy(photo = null) }
        persist()
        viewModelScope.launch { photoStore.delete(previous.filePath) }
    }

    private suspend fun replacePhoto(photo: SkinPhoto) {
        _uiState.update { it.copy(photo = photo) }
        persist()
        photoStore.pruneExcept(photo.filePath)
    }

    // ---- 03 Set Your Goals --------------------------------------------------

    fun toggleGoal(goal: SkinGoal) {
        val current = _uiState.value
        val selected = current.selectedGoals
        val next = when {
            goal in selected -> selected - goal
            selected.size >= SkinGoal.MAX_SELECTION -> {
                _uiState.update { it.copy(notice = OnboardingNotice.GOAL_LIMIT) }
                return
            }
            else -> selected + goal
        }
        val targets = if (targetCustomized) current.targetGoals else suggestTargetGoals(next)
        _uiState.update {
            it.copy(
                selectedGoals = next,
                targetGoals = targets,
                targetPhrase = buildTargetPhrase(next, targets),
                notice = null,
            )
        }
        persist()
    }

    // ---- 04 Your Target -----------------------------------------------------

    fun toggleTargetGoal(target: TargetGoal) {
        targetCustomized = true
        _uiState.update {
            val next = if (target in it.targetGoals) it.targetGoals - target else it.targetGoals + target
            it.copy(targetGoals = next, targetPhrase = buildTargetPhrase(it.selectedGoals, next))
        }
        persist()
    }

    // ---- 05 Skin Questionnaire ----------------------------------------------

    fun setAgeRange(ageRange: AgeRange) {
        _uiState.update { it.copy(ageRange = ageRange) }
        persist()
    }

    fun setSkinType(skinType: SkinType) {
        _uiState.update { it.copy(skinType = skinType) }
        persist()
    }

    fun toggleConcern(concern: SkinConcern) {
        _uiState.update {
            it.copy(concerns = if (concern in it.concerns) it.concerns - concern else it.concerns + concern)
        }
        persist()
    }

    // ---- 06 Lifestyle Factors -----------------------------------------------

    fun toggleLifestyleFactor(factor: LifestyleFactor) {
        _uiState.update {
            it.copy(
                lifestyleFactors = if (factor in it.lifestyleFactors) {
                    it.lifestyleFactors - factor
                } else {
                    it.lifestyleFactors + factor
                },
            )
        }
        persist()
    }

    fun finishOnboarding() {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            writeProfile()
            completeOnboarding()
            _uiState.update { it.copy(isSaving = false) }
            _events.send(OnboardingEvent.Completed)
        }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    private fun persist() {
        viewModelScope.launch { writeProfile() }
    }

    private suspend fun writeProfile() {
        val state = _uiState.value
        repository.updateSkinProfile { current ->
            current.copy(
                goals = state.selectedGoals,
                target = SkinTarget(phrase = state.targetPhrase, goals = state.targetGoals),
                ageRange = state.ageRange,
                skinType = state.skinType,
                concerns = state.concerns,
                lifestyleFactors = state.lifestyleFactors,
                baselinePhoto = state.photo,
            )
        }
    }
}
