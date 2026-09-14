package com.skinthesia.core.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.skinthesia.core.design.SkinthesiaPalette
import com.skinthesia.domain.model.ArticleCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ArtPalette(val top: Color, val bottom: Color, val ink: Color, val accent: Color)

private fun ArticleCategory.palette(): ArtPalette = when (this) {
    ArticleCategory.HYDRATION -> ArtPalette(Color(0xFFEEF3F4), Color(0xFFDCE6EA), SkinthesiaPalette.MistDeep, SkinthesiaPalette.Mist)
    ArticleCategory.SUNSCREEN -> ArtPalette(Color(0xFFF8EFDF), Color(0xFFEFDCC0), SkinthesiaPalette.GoldDeep, SkinthesiaPalette.Gold)
    ArticleCategory.INGREDIENTS -> ArtPalette(Color(0xFFF0F4EE), Color(0xFFDDE7DB), SkinthesiaPalette.SageDeep, SkinthesiaPalette.Sage)
    ArticleCategory.PIGMENTATION -> ArtPalette(Color(0xFFF6ECE6), Color(0xFFE8D2C6), SkinthesiaPalette.Clay, SkinthesiaPalette.ClaySoft)
    ArticleCategory.ACNE -> ArtPalette(Color(0xFFF8EDEA), Color(0xFFF0D9D3), SkinthesiaPalette.RoseDeep, SkinthesiaPalette.Rose)
    ArticleCategory.SKIN_HEALTH -> ArtPalette(Color(0xFFF7F0E8), Color(0xFFEADBCB), SkinthesiaPalette.Mocha, SkinthesiaPalette.BlushDeep)
    ArticleCategory.LIFESTYLE -> ArtPalette(Color(0xFFF1F2EA), Color(0xFFE3E4D2), SkinthesiaPalette.SageDeep, SkinthesiaPalette.Gold)
}

/**
 * Abstract editorial artwork per learning category: ripples for hydration, a low sun
 * for sunscreen, molecules for ingredients, dot fields for pigmentation, soft circles
 * for acne, skin layers for skin health and a crescent and leaf for lifestyle.
 */
@Composable
fun ArticleArtwork(
    category: ArticleCategory,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    contentDescription: String? = null,
) {
    val palette = category.palette()
    Canvas(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        drawRect(Brush.verticalGradient(listOf(palette.top, palette.bottom)))
        when (category) {
            ArticleCategory.HYDRATION -> ripples(palette)
            ArticleCategory.SUNSCREEN -> sun(palette)
            ArticleCategory.INGREDIENTS -> molecules(palette, seed)
            ArticleCategory.PIGMENTATION -> dots(palette, seed)
            ArticleCategory.ACNE -> bubbles(palette, seed)
            ArticleCategory.SKIN_HEALTH -> layers(palette)
            ArticleCategory.LIFESTYLE -> crescent(palette)
        }
    }
}

private fun DrawScope.ripples(p: ArtPalette) {
    val c = Offset(size.width * 0.62f, size.height * 0.72f)
    for (i in 1..7) {
        drawOval(p.ink.copy(alpha = 0.34f - i * 0.035f), topLeft = Offset(c.x - i * 26f * density, c.y - i * 9f * density), size = Size(i * 52f * density, i * 18f * density), style = Stroke(1.2f * density))
    }
    val d = Offset(size.width * 0.62f, size.height * 0.32f)
    val r = size.minDimension * 0.11f
    val drop = Path().apply {
        moveTo(d.x, d.y - r * 1.9f)
        cubicTo(d.x + r * 0.9f, d.y - r * 0.6f, d.x + r, d.y + r * 0.2f, d.x + r, d.y + r * 0.35f)
        cubicTo(d.x + r, d.y + r * 1.1f, d.x + r * 0.45f, d.y + r * 1.4f, d.x, d.y + r * 1.4f)
        cubicTo(d.x - r * 0.45f, d.y + r * 1.4f, d.x - r, d.y + r * 1.1f, d.x - r, d.y + r * 0.35f)
        cubicTo(d.x - r, d.y + r * 0.2f, d.x - r * 0.9f, d.y - r * 0.6f, d.x, d.y - r * 1.9f)
        close()
    }
    drawPath(drop, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f), p.accent.copy(alpha = 0.55f)), startY = d.y - r * 2, endY = d.y + r * 1.4f))
    drawPath(drop, p.ink.copy(alpha = 0.5f), style = Stroke(1.2f * density))
}

private fun DrawScope.sun(p: ArtPalette) {
    val horizon = size.height * 0.7f
    val c = Offset(size.width * 0.5f, horizon)
    val r = size.minDimension * 0.24f
    for (i in 1..5) drawCircle(p.accent.copy(alpha = 0.16f), radius = r + i * 14f * density, center = c, style = Stroke(1f * density))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFF9E3B8), p.accent), center = c, radius = r), radius = r, center = c)
    drawRect(p.bottom, topLeft = Offset(0f, horizon), size = Size(size.width, size.height - horizon))
    for (i in 0..4) {
        val y = horizon + (i + 1) * 9f * density
        val inset = size.width * (0.18f + i * 0.06f)
        drawLine(p.ink.copy(alpha = 0.28f - i * 0.04f), Offset(inset, y), Offset(size.width - inset, y), 1.2f * density, cap = StrokeCap.Round)
    }
}

private fun DrawScope.molecules(p: ArtPalette, seed: Int) {
    val r = size.minDimension * 0.12f
    val origin = Offset(size.width * 0.38f, size.height * 0.46f)
    val centers = listOf(origin, origin + Offset(r * 1.73f, 0f), origin + Offset(r * 0.87f, r * 1.5f), origin + Offset(r * 2.6f, r * 1.5f))
    centers.forEachIndexed { index, c ->
        val hex = Path()
        for (k in 0..5) {
            val a = PI / 3 * k + PI / 6
            val pt = Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat())
            if (k == 0) hex.moveTo(pt.x, pt.y) else hex.lineTo(pt.x, pt.y)
        }
        hex.close()
        drawPath(hex, p.ink.copy(alpha = 0.45f), style = Stroke(1.3f * density))
        if ((index + seed) % 2 == 0) drawCircle(p.accent.copy(alpha = 0.35f), radius = r * 0.45f, center = c)
    }
    val rnd = Random(seed + 3)
    centers.forEach { c ->
        val node = Offset(c.x + r * 1.6f * (rnd.nextFloat() - 0.2f), c.y - r * 1.4f * rnd.nextFloat())
        drawLine(p.ink.copy(alpha = 0.3f), c + Offset(0f, -r), node, 1f * density)
        drawCircle(p.surfaceDot(), radius = 4f * density, center = node)
        drawCircle(p.ink.copy(alpha = 0.55f), radius = 4f * density, center = node, style = Stroke(1.2f * density))
    }
}

private fun ArtPalette.surfaceDot(): Color = Color.White.copy(alpha = 0.9f)

private fun DrawScope.dots(p: ArtPalette, seed: Int) {
    val rnd = Random(seed + 11)
    val step = 14f * density
    var y = step
    while (y < size.height) {
        var x = step
        while (x < size.width) {
            val t = x / size.width
            val radius = (1.2f + 3.2f * t * rnd.nextFloat()) * density
            drawCircle(p.ink.copy(alpha = 0.12f + 0.35f * t * rnd.nextFloat()), radius = radius, center = Offset(x, y))
            x += step
        }
        y += step
    }
    drawCircle(Brush.radialGradient(listOf(p.accent.copy(alpha = 0.35f), Color.Transparent), center = Offset(size.width * 0.72f, size.height * 0.4f), radius = size.minDimension * 0.5f), radius = size.minDimension * 0.5f, center = Offset(size.width * 0.72f, size.height * 0.4f))
}

private fun DrawScope.bubbles(p: ArtPalette, seed: Int) {
    val rnd = Random(seed + 5)
    repeat(9) {
        val c = Offset(size.width * (0.2f + 0.65f * rnd.nextFloat()), size.height * (0.2f + 0.6f * rnd.nextFloat()))
        val r = size.minDimension * (0.05f + 0.13f * rnd.nextFloat())
        drawCircle(p.accent.copy(alpha = 0.16f), radius = r, center = c)
        drawCircle(p.ink.copy(alpha = 0.25f), radius = r, center = c, style = Stroke(1f * density))
    }
    for (i in 0..3) {
        val y = size.height * (0.78f + i * 0.05f)
        val path = Path().apply {
            moveTo(0f, y)
            cubicTo(size.width * 0.3f, y - 14f * density, size.width * 0.6f, y + 14f * density, size.width, y - 4f * density)
        }
        drawPath(path, p.ink.copy(alpha = 0.18f), style = Stroke(1f * density))
    }
}

private fun DrawScope.layers(p: ArtPalette) {
    val tones = listOf(Color(0xFFF3E6DA), Color(0xFFE9D3C3), Color(0xFFDDBBA8), Color(0xFFCFA38E))
    tones.forEachIndexed { i, tone ->
        val y = size.height * (0.38f + i * 0.15f)
        val path = Path().apply {
            moveTo(0f, y)
            cubicTo(size.width * 0.25f, y - 18f * density, size.width * 0.55f, y + 16f * density, size.width, y - 8f * density)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, tone)
        drawPath(Path().apply {
            moveTo(0f, y)
            cubicTo(size.width * 0.25f, y - 18f * density, size.width * 0.55f, y + 16f * density, size.width, y - 8f * density)
        }, p.ink.copy(alpha = 0.22f), style = Stroke(1f * density))
    }
    repeat(6) { i -> drawCircle(Color.White.copy(alpha = 0.5f), radius = 2.2f * density, center = Offset(size.width * (0.15f + i * 0.14f), size.height * 0.3f)) }
}

private fun DrawScope.crescent(p: ArtPalette) {
    val c = Offset(size.width * 0.3f, size.height * 0.36f)
    val r = size.minDimension * 0.16f
    drawCircle(p.accent.copy(alpha = 0.55f), radius = r, center = c)
    drawCircle(p.top, radius = r * 0.88f, center = c + Offset(r * 0.42f, -r * 0.2f))
    val stem = Offset(size.width * 0.66f, size.height * 0.84f)
    val tip = Offset(size.width * 0.8f, size.height * 0.34f)
    val leaf = Path().apply {
        moveTo(stem.x, stem.y)
        cubicTo(stem.x - size.width * 0.2f, stem.y - size.height * 0.3f, tip.x - size.width * 0.12f, tip.y + size.height * 0.05f, tip.x, tip.y)
        cubicTo(tip.x + size.width * 0.06f, tip.y + size.height * 0.2f, stem.x + size.width * 0.12f, stem.y - size.height * 0.15f, stem.x, stem.y)
        close()
    }
    drawPath(leaf, p.ink.copy(alpha = 0.2f))
    drawPath(leaf, p.ink.copy(alpha = 0.55f), style = Stroke(1.2f * density))
    drawLine(p.ink.copy(alpha = 0.45f), stem, tip, 1f * density)
    drawLine(p.ink.copy(alpha = 0.3f), Offset(0f, size.height * 0.9f), Offset(size.width, size.height * 0.9f), 1f * density)
}
