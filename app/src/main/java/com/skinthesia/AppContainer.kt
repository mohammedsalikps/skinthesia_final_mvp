package com.skinthesia

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.skinthesia.ai.analysis.MockSkinAnalysisEngine
import com.skinthesia.ai.analysis.ReportBuilder
import com.skinthesia.ai.analysis.SkinAnalysisEngine
import com.skinthesia.ai.progress.AdaptivePlanEngine
import com.skinthesia.ai.progress.JourneyClock
import com.skinthesia.ai.progress.ProgressAnalyzer
import com.skinthesia.ai.quality.OnDevicePhotoQualityAnalyzer
import com.skinthesia.ai.quality.PhotoQualityAnalyzer
import com.skinthesia.ai.recommendation.MockRecommendationEngine
import com.skinthesia.ai.recommendation.RecommendationEngine
import com.skinthesia.ai.skinprint.PotentialEstimator
import com.skinthesia.ai.vision.MockSkinVisionAnalyzer
import com.skinthesia.ai.vision.SkinVisionAnalyzer
import com.skinthesia.data.local.JsonPreferenceStore
import com.skinthesia.data.local.LocalImageRepository
import com.skinthesia.data.local.PhotoStorage
import com.skinthesia.data.local.db.SkinthesiaDatabase
import com.skinthesia.data.local.skinthesiaDataStore
import com.skinthesia.data.repository.DataStoreSettingsRepository
import com.skinthesia.data.repository.DataStoreUserProfileRepository
import com.skinthesia.data.repository.LocalCommunityRepository
import com.skinthesia.data.repository.LocalConsultationRepository
import com.skinthesia.data.repository.LocalLearningRepository
import com.skinthesia.data.repository.RoomAssessmentRepository
import com.skinthesia.data.repository.RoomCartRepository
import com.skinthesia.data.repository.RoomOrderRepository
import com.skinthesia.data.repository.RoomPlanRepository
import com.skinthesia.data.repository.RoomSkinProbeRepository
import com.skinthesia.data.repository.SeedProductCatalogRepository
import com.skinthesia.domain.model.AppSettings
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.CartRepository
import com.skinthesia.domain.repository.CommunityRepository
import com.skinthesia.domain.repository.ConsultationRepository
import com.skinthesia.domain.repository.LearningRepository
import com.skinthesia.domain.repository.OrderRepository
import com.skinthesia.domain.repository.PhotoStore
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.SkinthesiaImageRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.AttachPhotoUseCase
import com.skinthesia.domain.usecase.CompleteAssessmentUseCase
import com.skinthesia.domain.usecase.DataControlsUseCase
import com.skinthesia.domain.usecase.ProgressUseCase
import com.skinthesia.domain.usecase.RunAnalysisUseCase
import com.skinthesia.domain.usecase.StartAssessmentUseCase
import com.skinthesia.hardware.ble.MockBleDeviceProvider
import com.skinthesia.hardware.probe.MockSkinProbeManager
import com.skinthesia.hardware.probe.ProbeFailureInjector
import com.skinthesia.hardware.probe.SimulatedSensorDataProvider
import com.skinthesia.hardware.probe.SkinProbeManager
import com.skinthesia.hardware.probe.SwitchableSkinProbeManager
import com.skinthesia.hardware.probe.TcpSensorDataProvider
import com.skinthesia.hardware.wifi.EspProbeSocket
import com.skinthesia.hardware.wifi.WifiSocketDeviceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Flip to true only for on-device testing against a real Skinthesia ESP32 probe
 * (Wi-Fi SoftAP "Detector_Device", 192.168.4.1:5000 - see hardware/wifi/WifiSocketDeviceProvider.kt).
 * Left false so the app always ships using the safe, hardware-independent simulator.
 */
private const val USE_REAL_ESP32_PROBE = false

/**
 * Hand-written dependency graph. Every mock (probe, vision model, analysis engine,
 * recommender) and every local repository is chosen here and nowhere else, so real
 * implementations are a one-line swap in this file.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext
    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val clock: () -> Long = System::currentTimeMillis

    // Persistence
    private val database = SkinthesiaDatabase.build(appContext)
    private val dataStore = appContext.skinthesiaDataStore()

    val profiles: UserProfileRepository = DataStoreUserProfileRepository(
        JsonPreferenceStore(dataStore, "profile", UserProfile.serializer(), { UserProfile() }),
    )
    val settings: SettingsRepository = DataStoreSettingsRepository(
        JsonPreferenceStore(dataStore, "settings", AppSettings.serializer(), { AppSettings() }),
    )
    val assessments: AssessmentRepository = RoomAssessmentRepository(database.assessments())
    val probeRepository: SkinProbeRepository = RoomSkinProbeRepository(database.measurements())
    val plans: PlanRepository = RoomPlanRepository(database.plans())
    val catalog: ProductCatalogRepository = SeedProductCatalogRepository()
    val cart: CartRepository = RoomCartRepository(database.commerce())
    val orders: OrderRepository = RoomOrderRepository(database.commerce())
    val consultations: ConsultationRepository = LocalConsultationRepository(database.bookings())
    val learning: LearningRepository = LocalLearningRepository(database.library())
    val community: CommunityRepository = LocalCommunityRepository(database.community(), profiles, assessments)
    val photoStore: PhotoStore = PhotoStorage(appContext)
    val images: SkinthesiaImageRepository = LocalImageRepository()

    // AI (development implementations behind production interfaces)
    val qualityAnalyzer: PhotoQualityAnalyzer = OnDevicePhotoQualityAnalyzer()
    val visionAnalyzer: SkinVisionAnalyzer = MockSkinVisionAnalyzer()
    val analysisEngine: SkinAnalysisEngine = MockSkinAnalysisEngine()
    val recommendationEngine: RecommendationEngine = MockRecommendationEngine()
    val adaptivePlanEngine = AdaptivePlanEngine(recommendationEngine)
    val potentialEstimator = PotentialEstimator()
    val reportBuilder = ReportBuilder()
    val progressAnalyzer = ProgressAnalyzer()
    val journeyClock = JourneyClock()

    // Hardware: MockSkinProbeManager is the orchestrator either way - only which
    // BleDeviceProvider/SensorDataProvider pair it is given changes. See
    // USE_REAL_ESP32_PROBE above. mockProbeManager is always built: in real mode it
    // also doubles as the existing-simulator fallback a screen can switch to (via
    // SwitchableSkinProbeManager.useSimulated) if the real ESP32 can't be found.
    private val failureInjector = ProbeFailureInjector(appScope, settings)
    private val mockProbeManager: SkinProbeManager = MockSkinProbeManager(
        scope = appScope,
        ble = MockBleDeviceProvider(shouldFailConnection = { failureInjector.shouldFailConnection() }),
        sensors = SimulatedSensorDataProvider(contactLossRegion = failureInjector::contactLossRegion),
    )
    val probeManager: SkinProbeManager = if (USE_REAL_ESP32_PROBE) {
        val espProbeSocket = EspProbeSocket()
        val realProbeManager = MockSkinProbeManager(
            scope = appScope,
            ble = WifiSocketDeviceProvider(espProbeSocket),
            sensors = TcpSensorDataProvider(espProbeSocket),
        )
        SwitchableSkinProbeManager(scope = appScope, initial = realProbeManager, simulated = mockProbeManager)
    } else {
        mockProbeManager
    }

    // Use cases
    val progress = ProgressUseCase(assessments, probeRepository, plans, progressAnalyzer)
    val startAssessment = StartAssessmentUseCase(profiles, assessments, settings, journeyClock, clock)
    val attachPhoto = AttachPhotoUseCase(assessments, qualityAnalyzer, photoStore)
    val runAnalysis = RunAnalysisUseCase(profiles, assessments, probeRepository, visionAnalyzer, analysisEngine, clock)
    val completeAssessment = CompleteAssessmentUseCase(
        profiles, assessments, plans, catalog, recommendationEngine, adaptivePlanEngine, progress, clock,
    )
    val dataControls = DataControlsUseCase(
        profiles, settings, assessments, probeRepository, plans, cart, orders, consultations, community, photoStore,
    )
}

/** Gives composables access to the container without threading it through every call. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer has not been provided.")
}
