package com.skinthesia.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Raw Skinthesia palette derived from the reference artwork.
 *
 * Feature code should prefer the semantic roles on [SkinthesiaColorScheme]
 * over these raw values so the palette can evolve in one place.
 */
object SkinthesiaPalette {
    // Warm neutrals
    val Ivory = Color(0xFFF6F0E9)
    val Cream = Color(0xFFFCF9F5)
    val Linen = Color(0xFFF0E7DE)
    val Sand = Color(0xFFE6DACF)
    val SandDeep = Color(0xFFD6C6B8)
    val Parchment = Color(0xFFEFE3D8)

    // Blush family (header strips, selected states)
    val Blush = Color(0xFFEBD6CC)
    val BlushDeep = Color(0xFFDDBBAD)
    val BlushMist = Color(0xFFF4E8E1)

    // Browns (typography and primary actions)
    val Cocoa = Color(0xFF3E2A21)
    val Mocha = Color(0xFF6E5648)
    val Taupe = Color(0xFFA48F80)
    val TaupeLight = Color(0xFFC4B3A6)
    val Clay = Color(0xFF8A5C48)
    val ClayDeep = Color(0xFF6F493A)
    val ClaySoft = Color(0xFFB98C78)
    val ClayMist = Color(0xFFF1E2DA)

    // Restrained accents
    val Sage = Color(0xFF7C9782)
    val SageSoft = Color(0xFFE0E9DF)
    val Rose = Color(0xFFC78C85)
    val RoseSoft = Color(0xFFF3E1DD)
    val Gold = Color(0xFFC6A674)
    val GoldSoft = Color(0xFFF1E7D4)
    val Mist = Color(0xFF9AAEB8)
    val MistSoft = Color(0xFFE2E9EC)

    val White = Color(0xFFFFFFFF)
    val Ink = Color(0xFF1F1512)
}

/** Semantic color roles used throughout the app. */
@Immutable
data class SkinthesiaColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val surfaceTint: Color,
    val surfaceElevated: Color,
    val border: Color,
    val borderStrong: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnPrimary: Color,
    val textOnPhoto: Color,
    val primary: Color,
    val primaryPressed: Color,
    val primarySoft: Color,
    val primaryMist: Color,
    val accentBlush: Color,
    val accentBlushDeep: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val gold: Color,
    val goldSoft: Color,
    val scrim: Color,
    val photoOverlay: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
)

val SkinthesiaLightColors = SkinthesiaColorScheme(
    background = SkinthesiaPalette.Ivory,
    surface = SkinthesiaPalette.Cream,
    surfaceMuted = SkinthesiaPalette.Linen,
    surfaceTint = SkinthesiaPalette.Blush,
    surfaceElevated = SkinthesiaPalette.White,
    border = SkinthesiaPalette.Sand,
    borderStrong = SkinthesiaPalette.SandDeep,
    divider = SkinthesiaPalette.Parchment,
    textPrimary = SkinthesiaPalette.Cocoa,
    textSecondary = SkinthesiaPalette.Mocha,
    textMuted = SkinthesiaPalette.Taupe,
    textOnPrimary = SkinthesiaPalette.Cream,
    textOnPhoto = SkinthesiaPalette.White,
    primary = SkinthesiaPalette.Clay,
    primaryPressed = SkinthesiaPalette.ClayDeep,
    primarySoft = SkinthesiaPalette.ClaySoft,
    primaryMist = SkinthesiaPalette.ClayMist,
    accentBlush = SkinthesiaPalette.Blush,
    accentBlushDeep = SkinthesiaPalette.BlushDeep,
    success = SkinthesiaPalette.Sage,
    successSoft = SkinthesiaPalette.SageSoft,
    warning = SkinthesiaPalette.Rose,
    warningSoft = SkinthesiaPalette.RoseSoft,
    info = SkinthesiaPalette.Mist,
    infoSoft = SkinthesiaPalette.MistSoft,
    gold = SkinthesiaPalette.Gold,
    goldSoft = SkinthesiaPalette.GoldSoft,
    scrim = SkinthesiaPalette.Ink.copy(alpha = 0.32f),
    photoOverlay = SkinthesiaPalette.Ink.copy(alpha = 0.18f),
    disabledContainer = SkinthesiaPalette.SandDeep,
    disabledContent = SkinthesiaPalette.Cream.copy(alpha = 0.9f),
)

val LocalSkinthesiaColors = staticCompositionLocalOf { SkinthesiaLightColors }

/**
 * Bridges the Skinthesia roles into a Material 3 [ColorScheme] so that any
 * Material component used inside the app inherits the same warm palette.
 */
fun SkinthesiaColorScheme.toMaterialColorScheme(): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = textOnPrimary,
    primaryContainer = primaryMist,
    onPrimaryContainer = textPrimary,
    secondary = accentBlushDeep,
    onSecondary = textPrimary,
    secondaryContainer = accentBlush,
    onSecondaryContainer = textPrimary,
    tertiary = success,
    onTertiary = textOnPrimary,
    tertiaryContainer = successSoft,
    onTertiaryContainer = textPrimary,
    background = background,
    onBackground = textPrimary,
    surface = surface,
    onSurface = textPrimary,
    surfaceVariant = surfaceMuted,
    onSurfaceVariant = textSecondary,
    surfaceContainer = surface,
    surfaceContainerHigh = surfaceMuted,
    surfaceContainerHighest = surfaceMuted,
    surfaceContainerLow = surface,
    surfaceContainerLowest = surfaceElevated,
    outline = borderStrong,
    outlineVariant = border,
    error = warning,
    onError = textOnPrimary,
    errorContainer = warningSoft,
    onErrorContainer = textPrimary,
    scrim = scrim,
    inverseSurface = textPrimary,
    inverseOnSurface = surface,
    inversePrimary = primarySoft,
)
