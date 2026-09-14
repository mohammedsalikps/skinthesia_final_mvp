package com.skinthesia

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skinthesia.analysis.MockSkinAnalysisEngine
import com.skinthesia.analysis.SkinAnalysisEngine
import com.skinthesia.data.local.LocalImageRepository
import com.skinthesia.data.local.PhotoStorage
import com.skinthesia.data.local.PhotoStore
import com.skinthesia.data.local.UserProfileLocalDataSource
import com.skinthesia.data.repository.DataStoreUserProfileRepository
import com.skinthesia.domain.repository.SkinthesiaImageRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.BuildTargetPhraseUseCase
import com.skinthesia.domain.usecase.CompleteOnboardingUseCase
import com.skinthesia.domain.usecase.SuggestTargetGoalsUseCase
import com.skinthesia.feature.dashboard.HomeViewModel
import com.skinthesia.feature.onboarding.OnboardingViewModel

/**
 * Hand-rolled dependency graph. Small enough that a DI framework would add
 * more ceremony than it removes; swap implementations here to connect real
 * services (backend profile store, CDN imagery, hardware analysis).
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val photoStore: PhotoStore = PhotoStorage(appContext)

    val userProfileRepository: UserProfileRepository =
        DataStoreUserProfileRepository(UserProfileLocalDataSource(appContext))

    val imageRepository: SkinthesiaImageRepository = LocalImageRepository()

    val analysisEngine: SkinAnalysisEngine = MockSkinAnalysisEngine()

    val buildTargetPhrase = BuildTargetPhraseUseCase()
    val suggestTargetGoals = SuggestTargetGoalsUseCase()
    val completeOnboarding = CompleteOnboardingUseCase(userProfileRepository)

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { RootViewModel(userProfileRepository) }
        initializer {
            OnboardingViewModel(
                repository = userProfileRepository,
                photoStore = photoStore,
                buildTargetPhrase = buildTargetPhrase,
                suggestTargetGoals = suggestTargetGoals,
                completeOnboarding = completeOnboarding,
            )
        }
        initializer { HomeViewModel(userProfileRepository) }
    }
}

/** Gives composables access to the container without threading it through every call. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer has not been provided. Wrap content in CompositionLocalProvider(LocalAppContainer provides container).")
}
