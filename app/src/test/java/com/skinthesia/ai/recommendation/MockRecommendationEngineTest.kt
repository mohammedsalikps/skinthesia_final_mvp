package com.skinthesia.ai.recommendation

import com.skinthesia.ai.analysis.AnalysisInput
import com.skinthesia.ai.analysis.MockSkinAnalysisEngine
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.Budget
import com.skinthesia.domain.model.ProductAttribute
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.RoutineStepType
import com.skinthesia.domain.model.Sensitivity
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.StepFrequency
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockRecommendationEngineTest {

    private val engine = MockRecommendationEngine()

    private suspend fun analysed(profile: UserProfile): Assessment {
        val combined = MockSkinAnalysisEngine().analyze(
            AnalysisInput(Fixtures.assessment(), profile, Fixtures.session(), Fixtures.NOW),
        )
        return Fixtures.assessment(combined = combined)
    }

    @Test
    fun `plans are achievable with stable step ids`() = runTest {
        val profile = Fixtures.profile()
        val plan = engine.buildPlan(PlanInput(profile, analysed(profile), Fixtures.catalog, previous = null, now = Fixtures.NOW))
        assertEquals(
            listOf(RoutineStepType.CLEANSE, RoutineStepType.TREAT, RoutineStepType.MOISTURIZE, RoutineStepType.PROTECT),
            plan.morning.steps.map { it.type },
        )
        assertEquals(listOf("pm-cleanse", "pm-treat", "pm-moisturize"), plan.evening.steps.map { it.id })
        assertEquals(listOf(SkinGoal.HYDRATION, SkinGoal.DARK_SPOTS), plan.focus)
        assertEquals(1, plan.version)
    }

    @Test
    fun `the morning serum serves the first focus and every pick is explained`() = runTest {
        val profile = Fixtures.profile()
        val plan = engine.buildPlan(PlanInput(profile, analysed(profile), Fixtures.catalog, null, Fixtures.NOW))
        assertEquals("serum-hydra", plan.morning.steps.first { it.type == RoutineStepType.TREAT }.productId)
        assertEquals("treatment-tone", plan.evening.steps.first { it.type == RoutineStepType.TREAT }.productId)
        assertTrue(plan.recommendations.all { it.reasons.isNotEmpty() })
        assertTrue(plan.recommendations.first().reasons.any { it.text.contains("hydration focus") })
    }

    @Test
    fun `fragrance-free preference filters every pick`() = runTest {
        val profile = Fixtures.profile(preferences = setOf(Sensitivity.FRAGRANCE_FREE))
        val plan = engine.buildPlan(PlanInput(profile, analysed(profile), Fixtures.catalog, null, Fixtures.NOW))
        val byId = Fixtures.catalog.associateBy { it.id }
        assertTrue(plan.allSteps.all { ProductAttribute.FRAGRANCE_FREE in byId.getValue(it.productId!!).attributes })
    }

    @Test
    fun `existing products are respected`() = runTest {
        val profile = Fixtures.profile(currentProducts = setOf(ProductCategory.CLEANSER))
        val plan = engine.buildPlan(PlanInput(profile, analysed(profile), Fixtures.catalog, null, Fixtures.NOW))
        val cleanse = plan.morning.steps.first { it.type == RoutineStepType.CLEANSE }
        assertTrue(cleanse.keepCurrentProduct)
        assertEquals("Your current cleanser", cleanse.title)
    }

    @Test
    fun `evening retinal starts on alternate nights and is withheld from sensitive skin`() = runTest {
        val goals = listOf(SkinGoal.FINE_LINES, SkinGoal.TEXTURE)
        val profile = Fixtures.profile(goals = goals, priorities = listOf(SkinGoal.FINE_LINES), budget = Budget.PREMIUM)
        val plan = engine.buildPlan(PlanInput(profile, analysed(profile), Fixtures.catalog, null, Fixtures.NOW, focusOverride = listOf(SkinGoal.FINE_LINES)))
        val treat = plan.evening.steps.first { it.type == RoutineStepType.TREAT }
        assertEquals("treatment-retinal", treat.productId)
        assertEquals(StepFrequency.ALTERNATE_NIGHTS, treat.frequency)

        val sensitive = Fixtures.profile(goals = goals, priorities = listOf(SkinGoal.FINE_LINES), budget = Budget.PREMIUM, skinType = SkinType.SENSITIVE)
        val gentle = engine.buildPlan(PlanInput(sensitive, analysed(sensitive), Fixtures.catalog, null, Fixtures.NOW, focusOverride = listOf(SkinGoal.FINE_LINES)))
        assertTrue(gentle.evening.steps.none { it.productId == "treatment-retinal" })
    }

    @Test
    fun `ranking returns the catalogue sorted by fit`() {
        val ranked = engine.rank(Fixtures.profile(), null, Fixtures.catalog)
        assertTrue(ranked.zipWithNext().all { (a, b) -> a.matchScore >= b.matchScore })
    }
}
