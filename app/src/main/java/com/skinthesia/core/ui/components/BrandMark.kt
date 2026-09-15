package com.skinthesia.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme

/**
 * The official Skinthesia emblem (the copper ring-and-monogram mark), rendered from
 * the brand's own artwork - never redrawn or recolored. Used wherever the app needs
 * a compact, recognisable mark: headers, onboarding, loading and empty states.
 *
 * Set [elevated] when the mark sits over photography, a gradient or another busy
 * surface where the copper metal could lose contrast: it adds a soft, colourless
 * backing glow behind the emblem rather than touching the artwork itself.
 */
@Composable
fun BrandMonogram(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    elevated: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val description = stringResource(R.string.brand_monogram_cd)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (elevated) {
            Box(
                modifier = Modifier
                    .size(size * 1.32f)
                    .blur(size * 0.22f)
                    .background(colors.surface.copy(alpha = 0.78f), CircleShape),
            )
        }
        Image(
            painter = painterResource(R.drawable.brand_emblem),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .semantics { contentDescription = description },
        )
    }
}

/**
 * The full official lockup (emblem + "Skinthesia" wordmark) at its exact source
 * proportions, with the brand tagline set below in the app's own type. Pass a
 * stronger [taglineColor] when the lockup sits over photography, and [elevated]
 * when the artwork itself needs a soft backing to stay legible there.
 */
@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    width: Dp = 200.dp,
    showTagline: Boolean = true,
    taglineColor: Color = SkinthesiaTheme.colors.textMuted,
    elevated: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val name = stringResource(R.string.brand_name)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (elevated) {
                Box(
                    modifier = Modifier
                        .width(width * 1.12f)
                        .aspectRatio(LOCKUP_ASPECT)
                        .blur(width * 0.1f)
                        .background(colors.surface.copy(alpha = 0.72f), RoundedCornerShape(50)),
                )
            }
            Image(
                painter = painterResource(R.drawable.brand_lockup),
                contentDescription = null,
                modifier = Modifier
                    .width(width)
                    .aspectRatio(LOCKUP_ASPECT)
                    .semantics { contentDescription = name },
            )
        }
        if (showTagline) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.brand_tagline),
                style = typography.brandTagline,
                color = taglineColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Just the wordmark, for compact inline lockups (e.g. beside the emblem in a top bar). */
@Composable
fun BrandWordmark(modifier: Modifier = Modifier, height: Dp = 20.dp) {
    Image(
        painter = painterResource(R.drawable.brand_wordmark),
        contentDescription = null,
        modifier = modifier.height(height).aspectRatio(WORDMARK_ASPECT),
    )
}

/** Source lockup art is 1200x737px; wordmark-only crop is 1200x220px. */
private const val LOCKUP_ASPECT = 1200f / 737f
private const val WORDMARK_ASPECT = 1200f / 220f
