package com.skinthesia.feature.onboarding

import android.net.Uri
import com.skinthesia.data.local.PhotoStore
import com.skinthesia.domain.model.AgeRange
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.TargetGoal
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.BuildTargetPhraseUseCase
import com.skinthesia.domain.usecase.CompleteOnboardingUseCase
import com.skinthesia.domain.usecase.SuggestTargetGoalsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: FakeUserProfileRepository = FakeUserProfileRepository()) =
        OnboardingViewModel(
            repository = repository,
            photoStore = FakePhotoStore(),
            buildTargetPhrase = BuildTargetPhraseUseCase(),
            suggestTargetGoals = SuggestTargetGoalsUseCase(),
            completeOnboarding = CompleteOnboardingUseCase(repository),
        )

    @Test
    fun `caps goal selection at three and raises a notice`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.toggleGoal(SkinGoal.CLEARER_SKIN)
        viewModel.toggleGoal(SkinGoal.SMOOTHER_TEXTURE)
        viewModel.toggleGoal(SkinGoal.HEALTHY_GLOW)
        viewModel.toggleGoal(SkinGoal.LESS_REDNESS)

        val state = viewModel.uiState.value
        assertEquals(3, state.selectedGoals.size)
        assertFalse(SkinGoal.LESS_REDNESS in state.selectedGoals)
        assertEquals(OnboardingNotice.GOAL_LIMIT, state.notice)

        viewModel.dismissNotice()
        assertNull(viewModel.uiState.value.notice)
    }

    @Test
    fun `goals derive the target phrase and suggested targets`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.toggleGoal(SkinGoal.CLEARER_SKIN)

        val state = viewModel.uiState.value
        assertEquals("Clearer, healthier skin", state.targetPhrase)
        assertEquals(setOf(TargetGoal.REDUCE_BREAKOUTS), state.targetGoals)
    }

    @Test
    fun `manual target edits are kept when goals change`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.toggleGoal(SkinGoal.CLEARER_SKIN)
        viewModel.toggleTargetGoal(TargetGoal.BOOST_HYDRATION)
        viewModel.toggleGoal(SkinGoal.SMOOTHER_TEXTURE)

        assertEquals(
            setOf(TargetGoal.REDUCE_BREAKOUTS, TargetGoal.BOOST_HYDRATION),
            viewModel.uiState.value.targetGoals,
        )
    }

    @Test
    fun `answers persist to the repository`() = runTest(dispatcher) {
        val repository = FakeUserProfileRepository()
        val viewModel = createViewModel(repository)

        viewModel.setAgeRange(AgeRange.AGE_25_34)
        viewModel.setSkinType(SkinType.COMBINATION)
        viewModel.toggleConcern(SkinConcern.ACNE)

        val saved = repository.profile.first().skinProfile
        assertEquals(AgeRange.AGE_25_34, saved.ageRange)
        assertEquals(SkinType.COMBINATION, saved.skinType)
        assertEquals(setOf(SkinConcern.ACNE), saved.concerns)
        assertTrue(viewModel.uiState.value.canContinueFromQuestionnaire)
    }

    @Test
    fun `finishing onboarding completes the profile and emits an event`() = runTest(dispatcher) {
        val repository = FakeUserProfileRepository()
        val viewModel = createViewModel(repository)
        val events = mutableListOf<OnboardingEvent>()
        val collector = launch { viewModel.events.collect { events += it } }

        viewModel.finishOnboarding()

        assertTrue(repository.profile.first().onboardingCompleted)
        assertEquals(listOf<OnboardingEvent>(OnboardingEvent.Completed), events)
        assertFalse(viewModel.uiState.value.isSaving)
        collector.cancel()
    }

    @Test
    fun `restores previous answers on start`() = runTest(dispatcher) {
        val repository = FakeUserProfileRepository(
            initial = UserProfile(
                id = "existing",
                createdAt = 1L,
                skinProfile = SkinProfile(goals = listOf(SkinGoal.HEALTHY_GLOW), skinType = SkinType.DRY),
            ),
        )

        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value
        assertTrue(state.isLoaded)
        assertEquals(listOf(SkinGoal.HEALTHY_GLOW), state.selectedGoals)
        assertEquals(SkinType.DRY, state.skinType)
        assertEquals(setOf(TargetGoal.BOOST_HYDRATION), state.targetGoals)
    }
}

private class FakeUserProfileRepository(
    initial: UserProfile = UserProfile(id = "test", createdAt = 1L),
) : UserProfileRepository {

    private val state = MutableStateFlow(initial)

    override val profile: Flow<UserProfile> = state

    override suspend fun updateSkinProfile(transform: (SkinProfile) -> SkinProfile) {
        state.update { it.copy(skinProfile = transform(it.skinProfile)) }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        state.update { it.copy(onboardingCompleted = completed) }
    }

    override suspend fun setDisplayName(name: String?) {
        state.update { it.copy(displayName = name) }
    }
}

private class FakePhotoStore : PhotoStore {

    private val directory: File = Files.createTempDirectory("skinthesia-test").toFile()

    override fun newCaptureFile(): File = File(directory, "photo_${System.nanoTime()}.jpg")

    override suspend fun importFromUri(uri: Uri): File =
        throw UnsupportedOperationException("Gallery import is not exercised in unit tests")

    override suspend fun delete(path: String): Boolean = File(path).delete()

    override suspend fun pruneExcept(keepPath: String?) {
        directory.listFiles()?.filter { it.absolutePath != keepPath }?.forEach { it.delete() }
    }
}
