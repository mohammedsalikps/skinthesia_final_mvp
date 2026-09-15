package com.skinthesia.core.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.skinthesia.core.design.PlayfairDisplay
import com.skinthesia.core.design.SkinthesiaPalette
import com.skinthesia.domain.model.ProductForm
import com.skinthesia.domain.model.ProductTone
import kotlin.math.min

private data class ToneSpec(val light: Color, val dark: Color, val cap: Color, val label: Color, val ink: Color, val backdrop: Color)

private fun ProductTone.spec(): ToneSpec = when (this) {
    ProductTone.IVORY -> ToneSpec(Color(0xFFFBF6F0), Color(0xFFE2D4C6), SkinthesiaPalette.Clay, Color(0xFFF3EBE2), SkinthesiaPalette.Cocoa, Color(0xFFF2E6DC))
    ProductTone.BLUSH -> ToneSpec(Color(0xFFF2DDD3), Color(0xFFD8B0A1), SkinthesiaPalette.Cocoa, Color(0xFFFBF3EE), SkinthesiaPalette.Cocoa, Color(0xFFF6E7E0))
    ProductTone.SAGE -> ToneSpec(Color(0xFFE2EBE1), Color(0xFFB2C4B3), Color(0xFF5E7563), Color(0xFFF6F8F4), Color(0xFF3F5244), Color(0xFFE9EFE7))
    ProductTone.CLAY -> ToneSpec(Color(0xFFCB9D87), Color(0xFF94634F), Color(0xFF4E3328), Color(0xFFF4E6DD), SkinthesiaPalette.Cocoa, Color(0xFFF0DFD5))
    ProductTone.AMBER -> ToneSpec(Color(0xFFD9A874), Color(0xFF9C6636), Color(0xFF2F2320), Color(0xFFF7EEE2), SkinthesiaPalette.Cocoa, Color(0xFFF3E5D2))
    ProductTone.MIST -> ToneSpec(Color(0xFFE3EBEE), Color(0xFFB4C4CB), Color(0xFF5B707A), Color(0xFFF7F9FA), Color(0xFF3B4E57), Color(0xFFE8EEF0))
    ProductTone.SAND -> ToneSpec(Color(0xFFEFE3D4), Color(0xFFCDB6A0), Color(0xFF8A6E56), Color(0xFFFAF4EC), SkinthesiaPalette.Cocoa, Color(0xFFF3EADF))
}

/**
 * Drawn packaging for catalogue products (dropper, pump, tube, jar, bottle) in the
 * product's tone, with a soft backdrop and a small label mark. Keeps the marketplace
 * cohesive until licensed product photography is supplied.
 *
 * [mark] is the label's own initial (e.g. its brand's first letter) - never the
 * Skinthesia mark, so third-party products aren't mistaken for Skinthesia's own.
 * Leave it blank for a plain label.
 */
@Composable
fun ProductArtwork(
    form: ProductForm,
    tone: ProductTone,
    modifier: Modifier = Modifier,
    mark: String = "",
    backdrop: Boolean = true,
    contentDescription: String? = null,
) {
    val spec = tone.spec()
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        val vw = 100f
        val vh = 130f
        val scale = min(size.width / vw, size.height / vh)
        withTransform({
            translate((size.width - vw * scale) / 2, (size.height - vh * scale) / 2)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            if (backdrop) drawCircle(spec.backdrop, radius = 44f, center = Offset(50f, 70f))
            drawOval(Color.Black.copy(alpha = 0.08f), topLeft = Offset(24f, 118f), size = Size(52f, 6f))
            val labelRect = when (form) {
                ProductForm.DROPPER -> dropper(spec)
                ProductForm.PUMP -> pump(spec)
                ProductForm.TUBE -> tube(spec)
                ProductForm.JAR -> jar(spec)
                ProductForm.BOTTLE -> bottle(spec)
            }
            drawRoundRect(spec.label.copy(alpha = 0.92f), topLeft = labelRect.first, size = labelRect.second, cornerRadius = CornerRadius(3f))
            val layout = measurer.measure(mark, TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Medium, fontSize = (11f / scale * scale).sp, color = spec.ink))
            val textScale = (labelRect.second.height * 0.55f) / layout.size.height.coerceAtLeast(1)
            withTransform({
                translate(labelRect.first.x + labelRect.second.width / 2, labelRect.first.y + labelRect.second.height / 2)
                scale(textScale / scale * scale, textScale / scale * scale, pivot = Offset.Zero)
            }) {
                drawText(layout, topLeft = Offset(-layout.size.width / 2f, -layout.size.height / 2f))
            }
        }
    }
}

private fun DrawScope.body(spec: ToneSpec, topLeft: Offset, size: Size, radius: Float) {
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(spec.dark, spec.light, spec.light, spec.dark), startX = topLeft.x, endX = topLeft.x + size.width),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(radius),
    )
    drawRoundRect(Color.White.copy(alpha = 0.35f), topLeft = Offset(topLeft.x + size.width * 0.16f, topLeft.y + 6f), size = Size(2.2f, size.height - 12f), cornerRadius = CornerRadius(1f))
}

private fun DrawScope.cap(spec: ToneSpec, topLeft: Offset, size: Size, radius: Float = 3f) {
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(spec.cap.copy(alpha = 0.85f), spec.cap, spec.cap.copy(alpha = 0.75f)), startX = topLeft.x, endX = topLeft.x + size.width),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(radius),
    )
}

private fun DrawScope.dropper(spec: ToneSpec): Pair<Offset, Size> {
    body(spec, Offset(30f, 50f), Size(40f, 68f), 9f)
    drawRect(spec.dark, topLeft = Offset(41f, 42f), size = Size(18f, 9f))
    cap(spec, Offset(38f, 30f), Size(24f, 13f))
    drawRoundRect(spec.cap.copy(alpha = 0.9f), topLeft = Offset(44f, 12f), size = Size(12f, 20f), cornerRadius = CornerRadius(6f))
    return Offset(35f, 70f) to Size(30f, 30f)
}

private fun DrawScope.pump(spec: ToneSpec): Pair<Offset, Size> {
    body(spec, Offset(28f, 44f), Size(44f, 76f), 12f)
    cap(spec, Offset(39f, 33f), Size(22f, 12f))
    drawRect(spec.cap, topLeft = Offset(47f, 22f), size = Size(6f, 12f))
    cap(spec, Offset(38f, 15f), Size(26f, 8f))
    drawRoundRect(spec.cap, topLeft = Offset(62f, 17f), size = Size(12f, 4f), cornerRadius = CornerRadius(2f))
    return Offset(34f, 68f) to Size(32f, 32f)
}

private fun DrawScope.tube(spec: ToneSpec): Pair<Offset, Size> {
    val path = Path().apply {
        moveTo(26f, 20f)
        lineTo(74f, 20f)
        lineTo(64f, 104f)
        lineTo(36f, 104f)
        close()
    }
    drawPath(path, Brush.horizontalGradient(listOf(spec.dark, spec.light, spec.light, spec.dark), startX = 26f, endX = 74f))
    drawRect(spec.dark.copy(alpha = 0.8f), topLeft = Offset(26f, 14f), size = Size(48f, 7f))
    cap(spec, Offset(36f, 103f), Size(28f, 18f), 4f)
    return Offset(36f, 42f) to Size(28f, 30f)
}

private fun DrawScope.jar(spec: ToneSpec): Pair<Offset, Size> {
    body(spec, Offset(18f, 62f), Size(64f, 56f), 11f)
    cap(spec, Offset(15f, 45f), Size(70f, 19f), 6f)
    drawRoundRect(Color.White.copy(alpha = 0.18f), topLeft = Offset(20f, 48f), size = Size(60f, 3f), cornerRadius = CornerRadius(1.5f))
    return Offset(32f, 76f) to Size(36f, 28f)
}

private fun DrawScope.bottle(spec: ToneSpec): Pair<Offset, Size> {
    body(spec, Offset(32f, 42f), Size(36f, 78f), 10f)
    drawRect(spec.dark, topLeft = Offset(42f, 30f), size = Size(16f, 13f))
    cap(spec, Offset(40f, 12f), Size(20f, 20f), 4f)
    return Offset(36f, 66f) to Size(28f, 34f)
}
