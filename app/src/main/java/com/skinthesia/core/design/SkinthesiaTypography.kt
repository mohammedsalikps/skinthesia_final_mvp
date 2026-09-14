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
 * Inter (SIL Open Font License), the neutral sans used for body copy, labels,
 * buttons and navigation. Bundled as a variable font.
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
    lineBreak = LineBreak.Heading,
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
    /** The largest editorial statement, such as the Welcome headline. */
    val hero: TextStyle = serif(size = 38f, lineHeight = 44f, letterSpacing = -0.01f),
    /** Hero statements inside screens. */
    val displayLarge: TextStyle = serif(size = 34f, lineHeight = 40f, letterSpacing = -0.005f),
    /** Secondary hero line. */
    val display: TextStyle = serif(size = 26f, lineHeight = 32f),
    /** Quote-style statements such as the 12-week goal. Lines are balanced. */
    val displayItalic: TextStyle = serif(size = 25f, lineHeight = 34f, style = FontStyle.Italic),
    /** Large section statements. */
    val headline: TextStyle = serif(size = 28f, lineHeight = 34f),
    /** Screen titles. */
    val title: TextStyle = serif(size = 24f, lineHeight = 31f, weight = FontWeight.Medium),
    /** Card headlines. */
    val titleMedium: TextStyle = serif(size = 21f, lineHeight = 27f, weight = FontWeight.Medium),
    /** Card and section titles. */
    val titleSmall: TextStyle = serif(size = 18f, lineHeight = 24f, weight = FontWeight.Medium),
    /** Supporting copy under a title. */
    val subtitle: TextStyle = sans(size = 14f, lineHeight = 21f),
    /** Comfortable reading text for key sentences. */
    val bodyLarge: TextStyle = sans(size = 15.5f, lineHeight = 23f),
    /** Default reading text. */
    val body: TextStyle = sans(size = 14f, lineHeight = 21f),
    /** Dense reading text. */
    val bodySmall: TextStyle = sans(size = 12.5f, lineHeight = 18f),
    /** Long-form article body. */
    val articleBody: TextStyle = sans(size = 16f, lineHeight = 26f),
    /** Article standfirst. */
    val articleLead: TextStyle = serif(size = 19f, lineHeight = 28f, style = FontStyle.Italic),
    /** List titles and prominent labels. */
    val labelLarge: TextStyle = sans(size = 15f, lineHeight = 20f, weight = FontWeight.Medium),
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
    /** The SkinPrint score numeral. */
    val scoreNumeral: TextStyle = serif(size = 64f, lineHeight = 68f, letterSpacing = -0.02f),
    /** Hero metrics. */
    val metricLarge: TextStyle = serif(size = 44f, lineHeight = 48f, weight = FontWeight.Medium),
    /** Inline metrics. */
    val metric: TextStyle = serif(size = 22f, lineHeight = 26f, weight = FontWeight.Medium),
    /** Units and deltas next to a metric. */
    val metricUnit: TextStyle = sans(size = 11f, lineHeight = 14f, weight = FontWeight.Medium, letterSpacing = 0.04f),
    /** Tabular numbers in tables and rows. */
    val numeric: TextStyle = sans(size = 13f, lineHeight = 18f, weight = FontWeight.Medium).copy(fontFeatureSettings = "tnum"),
)

val LocalSkinthesiaTypography = staticCompositionLocalOf { SkinthesiaTypography() }

/** Bridges the Skinthesia scale into Material 3 so Material components stay on-brand. */
fun SkinthesiaTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = hero,
    displayMedium = display,
    displaySmall = displayItalic,
    headlineLarge = displayLarge,
    headlineMedium = title,
    headlineSmall = titleSmall,
    titleLarge = title,
    titleMedium = titleSmall,
    titleSmall = label,
    bodyLarge = bodyLarge,
    bodyMedium = body,
    bodySmall = bodySmall,
    labelLarge = button,
    labelMedium = label,
    labelSmall = overline,
)
