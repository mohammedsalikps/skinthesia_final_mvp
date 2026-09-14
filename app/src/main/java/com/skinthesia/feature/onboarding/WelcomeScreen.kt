package com.skinthesia.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.ContentMaxWidth
import com.skinthesia.core.ui.components.BenefitRow
import com.skinthesia.core.ui.components.BrandLockup
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.domain.model.ImageSource

/**
 * Screen 01. Full-bleed editorial photography carries the brand lockup at the
 * top and fades into cream behind the headline, with the promise and the
 * first call to action below, all on one screen.
 */
@Composable
fun WelcomeScreen(
    heroImage: ImageSource,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // Short screens and very large text fall back to a scrolling layout
        // with a fixed hero, so nothing is ever clipped.
        val fontScale = LocalDensity.current.fontScale
        val scrollMode = maxHeight < 620.dp || fontScale >= 1.4f
        val scrollHeroHeight = maxHeight * 0.62f

        if (scrollMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                WelcomeHero(
                    heroImage = heroImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(scrollHeroHeight),
                )
                WelcomeContent(onStart = onStart)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                WelcomeHero(
                    heroImage = heroImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                WelcomeContent(onStart = onStart)
            }
        }
    }
}

@Composable
private fun WelcomeHero(
    heroImage: ImageSource,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    Box(modifier = modifier) {
        SkinthesiaImage(
            source = heroImage,
            contentDescription = stringResource(R.string.welcome_hero_cd),
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.TopCenter,
        )
        // A soft cream veil at the top keeps the lockup legible over any photograph.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.40f)
                .background(
                    Brush.verticalGradient(
                        0f to colors.background.copy(alpha = 0.88f),
                        0.6f to colors.background.copy(alpha = 0.55f),
                        1f to colors.background.copy(alpha = 0f),
                    ),
                ),
        )
        // The photograph dissolves into the page behind the headline.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .background(
                    Brush.verticalGradient(
                        0f to colors.background.copy(alpha = 0f),
                        0.55f to colors.background.copy(alpha = 0.82f),
                        1f to colors.background,
                    ),
                ),
        )

        FadeInUp(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = spacing.lg),
        ) {
            BrandLockup(taglineColor = colors.textSecondary)
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
            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.welcome_headline),
                    style = typography.displayLarge,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.welcome_headline_secondary),
                    style = typography.display,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun WelcomeContent(onStart: () -> Unit) {
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
            .padding(top = spacing.md, bottom = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FadeInUp(delayMillis = motion.stagger(3)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    BenefitRow(text = stringResource(R.string.welcome_benefit_personalized))
                    BenefitRow(text = stringResource(R.string.welcome_benefit_progress))
                    BenefitRow(text = stringResource(R.string.welcome_benefit_expert))
                    BenefitRow(text = stringResource(R.string.welcome_benefit_community))
                }
            }

            Spacer(Modifier.height(spacing.lg))

            FadeInUp(delayMillis = motion.stagger(4)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SkinthesiaPrimaryButton(
                        text = stringResource(R.string.welcome_cta),
                        onClick = onStart,
                    )
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = stringResource(R.string.welcome_footnote),
                        style = typography.caption,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
