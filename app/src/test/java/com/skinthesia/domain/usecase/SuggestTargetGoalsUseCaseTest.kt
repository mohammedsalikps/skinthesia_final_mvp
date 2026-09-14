package com.skinthesia.domain.usecase

import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.TargetGoal
import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestTargetGoalsUseCaseTest {

    private val useCase = SuggestTargetGoalsUseCase()

    @Test
    fun `no goals suggests every measurable target`() {
        assertEquals(TargetGoal.entries.toSet(), useCase(emptyList()))
    }

    @Test
    fun `goals map onto their measurable targets`() {
        val targets = useCase(listOf(SkinGoal.CLEARER_SKIN, SkinGoal.HEALTHY_GLOW))
        assertEquals(setOf(TargetGoal.REDUCE_BREAKOUTS, TargetGoal.BOOST_HYDRATION), targets)
    }

    @Test
    fun `related goals collapse into a single target`() {
        val targets = useCase(listOf(SkinGoal.SMOOTHER_TEXTURE, SkinGoal.REDUCE_FINE_LINES))
        assertEquals(setOf(TargetGoal.IMPROVE_TEXTURE), targets)
    }
}
