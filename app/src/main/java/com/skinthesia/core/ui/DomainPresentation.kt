package com.skinthesia.core.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.skinthesia.R
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AgeRange
import com.skinthesia.domain.model.LifestyleFactor
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.TargetGoal

/*
 * Presentation mappings from domain enums to localized labels and icons.
 * Keeping these here leaves the domain layer free of Android resources.
 */

val SkinGoal.labelRes: Int
    @StringRes get() = when (this) {
        SkinGoal.CLEARER_SKIN -> R.string.goal_clearer_skin
        SkinGoal.FEWER_BREAKOUTS -> R.string.goal_fewer_breakouts
        SkinGoal.EVEN_SKIN_TONE -> R.string.goal_even_skin_tone
        SkinGoal.SMOOTHER_TEXTURE -> R.string.goal_smoother_texture
        SkinGoal.REDUCE_DARK_SPOTS -> R.string.goal_reduce_dark_spots
        SkinGoal.HEALTHY_GLOW -> R.string.goal_healthy_glow
        SkinGoal.STRONGER_BARRIER -> R.string.goal_stronger_barrier
        SkinGoal.LESS_REDNESS -> R.string.goal_less_redness
        SkinGoal.REDUCE_FINE_LINES -> R.string.goal_reduce_fine_lines
    }

val SkinGoal.icon: ImageVector
    get() = when (this) {
        SkinGoal.CLEARER_SKIN -> SkinthesiaIcons.Sparkle
        SkinGoal.FEWER_BREAKOUTS -> SkinthesiaIcons.Breakouts
        SkinGoal.EVEN_SKIN_TONE -> SkinthesiaIcons.EvenTone
        SkinGoal.SMOOTHER_TEXTURE -> SkinthesiaIcons.Texture
        SkinGoal.REDUCE_DARK_SPOTS -> SkinthesiaIcons.DarkSpots
        SkinGoal.HEALTHY_GLOW -> SkinthesiaIcons.Glow
        SkinGoal.STRONGER_BARRIER -> SkinthesiaIcons.Shield
        SkinGoal.LESS_REDNESS -> SkinthesiaIcons.Calm
        SkinGoal.REDUCE_FINE_LINES -> SkinthesiaIcons.FineLines
    }

val TargetGoal.labelRes: Int
    @StringRes get() = when (this) {
        TargetGoal.REDUCE_BREAKOUTS -> R.string.target_reduce_breakouts
        TargetGoal.FADE_DARK_SPOTS -> R.string.target_fade_dark_spots
        TargetGoal.IMPROVE_TEXTURE -> R.string.target_improve_texture
        TargetGoal.BOOST_HYDRATION -> R.string.target_boost_hydration
    }

val AgeRange.labelRes: Int
    @StringRes get() = when (this) {
        AgeRange.UNDER_18 -> R.string.age_under_18
        AgeRange.AGE_18_24 -> R.string.age_18_24
        AgeRange.AGE_25_34 -> R.string.age_25_34
        AgeRange.AGE_35_44 -> R.string.age_35_44
        AgeRange.AGE_45_54 -> R.string.age_45_54
        AgeRange.AGE_55_PLUS -> R.string.age_55_plus
    }

val SkinType.labelRes: Int
    @StringRes get() = when (this) {
        SkinType.NORMAL -> R.string.skin_type_normal
        SkinType.DRY -> R.string.skin_type_dry
        SkinType.OILY -> R.string.skin_type_oily
        SkinType.COMBINATION -> R.string.skin_type_combination
        SkinType.SENSITIVE -> R.string.skin_type_sensitive
    }

val SkinConcern.labelRes: Int
    @StringRes get() = when (this) {
        SkinConcern.ACNE -> R.string.concern_acne
        SkinConcern.PIGMENTATION -> R.string.concern_pigmentation
        SkinConcern.TEXTURE -> R.string.concern_texture
        SkinConcern.DRYNESS -> R.string.concern_dryness
        SkinConcern.REDNESS -> R.string.concern_redness
        SkinConcern.FINE_LINES -> R.string.concern_fine_lines
        SkinConcern.DULLNESS -> R.string.concern_dullness
        SkinConcern.ENLARGED_PORES -> R.string.concern_pores
    }

val LifestyleFactor.labelRes: Int
    @StringRes get() = when (this) {
        LifestyleFactor.STRESS -> R.string.factor_stress
        LifestyleFactor.SLEEP -> R.string.factor_sleep
        LifestyleFactor.DIET -> R.string.factor_diet
        LifestyleFactor.SUN_EXPOSURE -> R.string.factor_sun
        LifestyleFactor.WORK_ENVIRONMENT -> R.string.factor_work
        LifestyleFactor.HORMONAL_CHANGES -> R.string.factor_hormonal
        LifestyleFactor.SKINCARE_PRODUCTS -> R.string.factor_products
        LifestyleFactor.CLIMATE -> R.string.factor_climate
        LifestyleFactor.ILLNESS_MEDICATION -> R.string.factor_illness
    }

val LifestyleFactor.icon: ImageVector
    get() = when (this) {
        LifestyleFactor.STRESS -> SkinthesiaIcons.Stress
        LifestyleFactor.SLEEP -> SkinthesiaIcons.Sleep
        LifestyleFactor.DIET -> SkinthesiaIcons.Diet
        LifestyleFactor.SUN_EXPOSURE -> SkinthesiaIcons.Sun
        LifestyleFactor.WORK_ENVIRONMENT -> SkinthesiaIcons.Work
        LifestyleFactor.HORMONAL_CHANGES -> SkinthesiaIcons.Hormonal
        LifestyleFactor.SKINCARE_PRODUCTS -> SkinthesiaIcons.Products
        LifestyleFactor.CLIMATE -> SkinthesiaIcons.Climate
        LifestyleFactor.ILLNESS_MEDICATION -> SkinthesiaIcons.Illness
    }
