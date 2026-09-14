package com.skinthesia.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme

private const val MONOGRAM_GRID = 24f
private const val MONOGRAM_S =
    "M15.4,8.6 C15,7.3 13.7,6.5 12.1,6.5 C10.3,6.5 8.9,7.5 8.9,9 C8.9,12.3 15.3,10.6 15.3,14.6 " +
        "C15.3,16.4 13.7,17.5 11.9,17.5 C10.2,17.5 8.9,16.7 8.5,15.4"

/** The circular "S" monogram used in the app bar, splash and launcher. */
@Composable
fun BrandMonogram(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = SkinthesiaTheme.colors.primary,
    background: Color = Color.Transparent,
) {
    val description = stringResource(R.string.brand_monogram_cd)
    val monogramPath = remember { PathParser().parsePathString(MONOGRAM_S).toPath() }
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = description },
    ) {
        val scale = this.size.minDimension / MONOGRAM_GRID
        if (background != Color.Transparent) {
            drawCircle(color = background)
        }
        drawCircle(
            color = tint,
            radius = 9.5f * scale,
            style = Stroke(width = 1.1f * scale),
        )
        withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {
            drawPath(
                path = monogramPath,
                color = tint,
                style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

/**
 * Monogram, wordmark and tagline stacked as on the Welcome screen. Pass a
 * stronger [taglineColor] when the lockup sits over photography.
 */
@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    monogramSize: Dp = 44.dp,
    showTagline: Boolean = true,
    taglineColor: Color = SkinthesiaTheme.colors.textMuted,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandMonogram(size = monogramSize)
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.brand_name),
            style = typography.brand,
            color = colors.primary,
            textAlign = TextAlign.Center,
        )
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
