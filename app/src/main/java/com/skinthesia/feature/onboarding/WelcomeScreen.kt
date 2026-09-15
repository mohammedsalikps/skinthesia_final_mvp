package com.skinthesia.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skinthesia.LocalAppContainer
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.ContentMaxWidth
import com.skinthesia.core.ui.components.BrandLockup
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.repository.ImageKey

/**
 * Screen 01. Full-bleed editorial light with the brand lockup above and the promise
 * resting on a cream fade, then one clear call to action. Minimal by design.
 */
@Composable
fun WelcomeScreen(onStart: () -> Unit, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val hero = remember(container) { container.images.image(ImageKey.WELCOME_HERO) }
    WelcomeContent(hero = hero, onStart = onStart, modifier = modifier)
}

@Composable
fun WelcomeContent(hero: ImageSource, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        val fontScale = LocalDensity.current.fontScale
        val scrollMode = maxHeight < 640.dp || fontScale >= 1.35f
        val scrollHeroHeight = maxHeight * 0.58f
        if (scrollMode) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                WelcomeHero(hero, Modifier.fillMaxWidth().height(scrollHeroHeight))
                WelcomeFooter(onStart)
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                WelcomeHero(hero, Modifier.fillMaxWidth().weight(1f))
                WelcomeFooter(onStart)
            }
        }
    }
}

@Composable
private fun WelcomeHero(hero: ImageSource, modifier: Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    Box(modifier = modifier) {
        SkinthesiaImage(source = hero, contentDescription = null, modifier = Modifier.fillMaxSize(), alignment = Alignment.TopCenter)
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.38f)
                .background(Brush.verticalGradient(0f to colors.background.copy(alpha = 0.85f), 0.6f to colors.background.copy(alpha = 0.45f), 1f to colors.background.copy(alpha = 0f))),
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .background(Brush.verticalGradient(0f to colors.background.copy(alpha = 0f), 0.6f to colors.background.copy(alpha = 0.85f), 1f to colors.background)),
        )
        FadeInUp(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = spacing.lg)) {
            BrandLockup(width = 176.dp, taglineColor = colors.textSecondary, elevated = true)
        }
        FadeInUp(
            delayMillis = motion.stagger(2),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = spacing.screenHorizontal)
                .padding(bottom = spacing.xs),
        ) {
            Text(
                text = "Science for\nhealthier skin.",
                style = typography.hero,
                color = colors.textPrimary,
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .semantics { heading() },
            )
        }
    }
}

@Composable
private fun WelcomeFooter(onStart: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .navigationBarsPadding()
            .padding(horizontal = spacing.screenHorizontal)
            .padding(top = spacing.sm, bottom = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth()) {
            FadeInUp(delayMillis = motion.stagger(3)) {
                Text(
                    text = "Your camera, a gentle skin probe and your goals, combined into SkinPrint™, a personal skin profile that learns with you.",
                    style = typography.bodyLarge,
                    color = colors.textSecondary,
                )
            }
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = motion.stagger(4)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf("See", "Measure", "Understand", "Improve").forEachIndexed { index, word ->
                        if (index > 0) Box(Modifier.size(3.dp).clip(CircleShape).background(colors.primarySoft))
                        Text(text = word.uppercase(), style = typography.overline, color = colors.textMuted)
                    }
                }
            }
            Spacer(Modifier.height(spacing.lg))
            FadeInUp(delayMillis = motion.stagger(5)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    SkinthesiaPrimaryButton(text = "Start My Journey", onClick = onStart)
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = "Wellness guidance, not a medical diagnosis.",
                        style = typography.caption,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
