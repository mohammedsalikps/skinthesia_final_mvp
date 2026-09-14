package com.skinthesia.core.design

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.skinthesia.R

/**
 * Playfair Display (SIL Open Font License), the editorial serif used for the
 * wordmark, headlines and metric numerals. Bundled as a variable font.
 */
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display, weight = FontWeight.Normal),
    Font(R.font.playfair_display, weight = FontWeight.Medium),
    Font(R.font.playfair_display, weight = FontWeight.SemiBold),
    Font(R.font.playfair_display_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.playfair_display_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
)

/**
 * Inter (SIL Open Font License), the neutral sans used for body copy,
 * labels, buttons and navigation. Bundled as a variable font.
 */
val Inter = FontFamily(
    Font(R.font.inter, weight = FontWeight.Normal),
    Font(R.font.inter, weight = FontWeight.Medium),
    Font(R.font.inter, weight = FontWeight.SemiBold),
)

private val editorialLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private val noPadding = PlatformTextStyle(includeFontPadding = false)

private fun serif(
    size: Float,
    lineHeight: Float,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = PlayfairDisplay,
    fontWeight = weight,
    fontStyle = style,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.em,
    platformStyle = noPadding,
    lineHeightStyle = editorialLineHeightStyle,
)

private fun sans(
    size: Float,
    lineHeight: Float,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.em,
    platformStyle = noPadding,
    lineHeightStyle = editorialLineHeightStyle,
)

/** The Skinthesia type scale. Every text in the app maps to one of these roles. */
@Immutable
data class SkinthesiaTypography(
    /** "Skinthesia" wordmark. */
    val brand: TextStyle = serif(size = 26f, lineHeight = 30f, weight = FontWeight.Medium, letterSpacing = 0.005f),
    /** "SCIENCE FOR HEALTHY SKIN" tagline under the wordmark. */
    val brandTagline: TextStyle = sans(size = 9.5f, lineHeight = 12f, weight = FontWeight.Medium, letterSpacing = 0.24f),
    /** Hero statements such as "Healthier skin". */
    val displayLarge: TextStyle = serif(size = 34f, lineHeight = 40f, weight = FontWeight.Normal, letterSpacing = -0.005f),
    /** Secondary hero line such as "A more confident you." */
    val display: TextStyle = serif(size = 26f, lineHeight = 32f, weight = FontWeight.Normal),
    /** Quote-style statements such as the 12-week target. Lines are balanced so no word is orphaned. */
    val displayItalic: TextStyle = serif(size = 25f, lineHeight = 34f, weight = FontWeight.Normal, style = FontStyle.Italic)
        .copy(lineBreak = LineBreak.Heading),
    /** Screen titles. */
    val title: TextStyle = serif(size = 24f, lineHeight = 31f, weight = FontWeight.Medium),
    /** Card and section titles. */
    val titleSmall: TextStyle = serif(size = 18f, lineHeight = 24f, weight = FontWeight.Medium),
    /** Supporting copy under a title. */
    val subtitle: TextStyle = sans(size = 14f, lineHeight = 21f),
    /** Default reading text. */
    val body: TextStyle = sans(size = 14f, lineHeight = 21f),
    /** Dense reading text. */
    val bodySmall: TextStyle = sans(size = 12.5f, lineHeight = 18f),
    /** Emphasised UI labels (tiles, rows, fields). */
    val label: TextStyle = sans(size = 13f, lineHeight = 18f, weight = FontWeight.Medium),
    /** Small labels (tile captions). */
    val labelSmall: TextStyle = sans(size = 11.5f, lineHeight = 15f, weight = FontWeight.Medium),
    /** Footnotes and helper text. */
    val caption: TextStyle = sans(size = 11f, lineHeight = 15f),
    /** Uppercase micro-labels with generous tracking. */
    val overline: TextStyle = sans(size = 10f, lineHeight = 13f, weight = FontWeight.Medium, letterSpacing = 0.18f),
    /** Primary and secondary buttons. */
    val button: TextStyle = sans(size = 15f, lineHeight = 20f, weight = FontWeight.SemiBold, letterSpacing = 0.01f),
    /** Text buttons and inline links. */
    val buttonSmall: TextStyle = sans(size = 13f, lineHeight = 18f, weight = FontWeight.Medium),
    /** Bottom navigation labels. */
    val navigation: TextStyle = sans(size = 10f, lineHeight = 12f, weight = FontWeight.Medium, letterSpacing = 0.02f),
    /** Hero metrics such as the SkinPrint score. */
    val metricLarge: TextStyle = serif(size = 46f, lineHeight = 50f, weight = FontWeight.Medium),
    /** Inline metrics such as "72". */
    val metric: TextStyle = serif(size = 22f, lineHeight = 26f, weight = FontWeight.Medium),
    /** Units and deltas next to a metric. */
    val metricUnit: TextStyle = sans(size = 11f, lineHeight = 14f, weight = FontWeight.Medium, letterSpacing = 0.04f),
)

val LocalSkinthesiaTypography = staticCompositionLocalOf { SkinthesiaTypography() }

/** Bridges the Skinthesia scale into Material 3 so Material components stay on-brand. */
fun SkinthesiaTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = displayLarge,
    displayMedium = display,
    displaySmall = displayItalic,
    headlineLarge = displayLarge,
    headlineMedium = title,
    headlineSmall = titleSmall,
    titleLarge = title,
    titleMedium = titleSmall,
    titleSmall = label,
    bodyLarge = body,
    bodyMedium = body,
    bodySmall = bodySmall,
    labelLarge = button,
    labelMedium = label,
    labelSmall = overline,
)
