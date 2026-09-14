package com.skinthesia.domain.usecase

import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.TargetGoal
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildTargetPhraseUseCaseTest {

    private val useCase = BuildTargetPhraseUseCase()

    @Test
    fun `empty selection falls back to the default phrase`() {
        assertEquals("Clearer, healthier skin", useCase(emptyList(), emptySet()))
    }

    @Test
    fun `single goal produces a two part phrase`() {
        assertEquals("Smoother, healthier skin", useCase(listOf(SkinGoal.SMOOTHER_TEXTURE), emptySet()))
    }

    @Test
    fun `two descriptors are joined with and`() {
        val phrase = useCase(listOf(SkinGoal.CLEARER_SKIN, SkinGoal.REDUCE_DARK_SPOTS), emptySet())
        assertEquals("Clearer, brighter and healthier skin", phrase)
    }

    @Test
    fun `duplicate adjectives collapse into one`() {
        val phrase = useCase(
            listOf(SkinGoal.CLEARER_SKIN, SkinGoal.FEWER_BREAKOUTS),
            setOf(TargetGoal.REDUCE_BREAKOUTS),
        )
        assertEquals("Clearer, healthier skin", phrase)
    }

    @Test
    fun `targets contribute when no goals are selected`() {
        assertEquals("More hydrated, healthier skin", useCase(emptyList(), setOf(TargetGoal.BOOST_HYDRATION)))
    }

    @Test
    fun `phrase never carries more than two descriptors`() {
        val phrase = useCase(
            listOf(SkinGoal.CLEARER_SKIN, SkinGoal.SMOOTHER_TEXTURE, SkinGoal.STRONGER_BARRIER),
            setOf(TargetGoal.BOOST_HYDRATION),
        )
        assertEquals("Clearer, smoother and healthier skin", phrase)
    }
}
