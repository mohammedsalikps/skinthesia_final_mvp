package com.skinthesia.core.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Hand-drawn thin-line icon set on a 24 unit grid, matching the fine, calm
 * line work of the reference artwork. Icons are single-colour and take the
 * tint passed to [androidx.compose.material3.Icon].
 */
object SkinthesiaIcons {

    // Navigation and actions
    val ArrowRight: ImageVector by lazy { lineIcon("ArrowRight", "M5 12h14", "M13 6l6 6-6 6") }
    val ArrowLeft: ImageVector by lazy { lineIcon("ArrowLeft", "M19 12H5", "M11 6l-6 6 6 6") }
    val Check: ImageVector by lazy { lineIcon("Check", "M5 12.5l4.5 4.5L19 7", strokeWidth = 1.8f) }
    val ChevronDown: ImageVector by lazy { lineIcon("ChevronDown", "M6 9.5l6 6 6-6") }
    val ChevronRight: ImageVector by lazy { lineIcon("ChevronRight", "M9.5 6l6 6-6 6") }
    val Close: ImageVector by lazy { lineIcon("Close", "M6 6l12 12", "M18 6L6 18") }
    val Edit: ImageVector by lazy {
        lineIcon("Edit", "M4 20h4l10.5-10.5a1.5 1.5 0 0 0 0-2.1l-1.9-1.9a1.5 1.5 0 0 0-2.1 0L4 16z", "M13.5 6.5l4 4")
    }
    val Info: ImageVector by lazy { lineIcon("Info", circle(12f, 12f, 8.5f), "M12 11v5", "M12 8v0.5") }
    val Bell: ImageVector by lazy {
        lineIcon("Bell", "M6.5 16.5v-5.5a5.5 5.5 0 0 1 11 0v5.5l1.5 2h-14z", "M10 20.5a2 2 0 0 0 4 0")
    }

    // Camera
    val Camera: ImageVector by lazy {
        lineIcon(
            "Camera",
            "M4 8.5A1.5 1.5 0 0 1 5.5 7h2.2l1.4-2h5.8l1.4 2h2.2A1.5 1.5 0 0 1 20 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5z",
            circle(12f, 12.5f, 3.2f),
        )
    }
    val Retake: ImageVector by lazy { lineIcon("Retake", "M5.6 8A7.5 7.5 0 1 1 4.5 12", "M4.5 4.5V8H8") }
    val Flip: ImageVector by lazy {
        lineIcon(
            "Flip",
            "M4 12a8 8 0 0 1 13.7-5.6",
            "M20 12a8 8 0 0 1-13.7 5.6",
            "M17.7 2.4v4h-4",
            "M6.3 21.6v-4h4",
        )
    }
    val Gallery: ImageVector by lazy {
        lineIcon(
            "Gallery",
            "M4 5.5A1.5 1.5 0 0 1 5.5 4h13A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5z",
            "M4 16l4.5-4.5 4 4 3-3L20 17",
            fills = listOf(circle(15.5f, 9f, 1.3f)),
        )
    }

    // Goals
    val Sparkle: ImageVector by lazy {
        lineIcon(
            "Sparkle",
            "M12 3.5l1.9 5.6 5.6 1.9-5.6 1.9L12 18.5l-1.9-5.6L4.5 11l5.6-1.9z",
            "M18.5 16.5l0.7 2 2 0.7-2 0.7-0.7 2-0.7-2-2-0.7 2-0.7z",
            strokeWidth = 1.3f,
        )
    }
    val Breakouts: ImageVector by lazy {
        lineIcon("Breakouts", circle(8f, 10.5f, 2.2f), circle(15.5f, 9.5f, 2.6f), circle(12f, 16.5f, 2f))
    }
    val EvenTone: ImageVector by lazy {
        lineIcon("EvenTone", "M5 5h5.5v5.5H5z", "M13.5 5H19v5.5h-5.5z", "M5 13.5h5.5V19H5z", "M13.5 13.5H19V19h-5.5z", strokeWidth = 1.3f)
    }
    val Texture: ImageVector by lazy {
        lineIcon("Texture", "M3.5 8.5c2.8-3 5.7 3 8.5 0s5.7-3 8.5 0", "M3.5 15.5c2.8-3 5.7 3 8.5 0s5.7-3 8.5 0")
    }
    val DarkSpots: ImageVector by lazy {
        lineIcon(
            "DarkSpots",
            circle(12f, 12f, 8.5f),
            fills = listOf(circle(9f, 10f, 1.3f), circle(14.5f, 12.5f, 1.1f), circle(11f, 15.3f, 0.9f)),
        )
    }
    val Glow: ImageVector by lazy {
        lineIcon(
            "Glow",
            circle(12f, 12f, 3.5f),
            "M12 3.5v2", "M12 18.5v2", "M3.5 12h2", "M18.5 12h2",
            "M6 6l1.4 1.4", "M16.6 16.6L18 18", "M6 18l1.4-1.4", "M16.6 7.4L18 6",
        )
    }
    val Shield: ImageVector by lazy {
        lineIcon("Shield", "M12 3.5l7 2.6v5.4c0 4.6-3 8-7 9.5-4-1.5-7-4.9-7-9.5V6.1z", "M9.5 12.2l1.8 1.8 3.4-3.6")
    }
    val Calm: ImageVector by lazy {
        lineIcon("Calm", "M12 4c2.3 3 6 5.6 6 10a6 6 0 0 1-12 0c0-2.2 1-3.6 2.2-4.8 0.4 1.2 1.1 2 2 2.4C10 8.8 10.8 6.4 12 4z")
    }
    val FineLines: ImageVector by lazy {
        lineIcon("FineLines", "M5 8.5c4.5-2 9.5-2 14 0", "M6.5 12.5c3.7-1.5 7.3-1.5 11 0", "M8 16.5c2.7-1 5.3-1 8 0")
    }

    // Lifestyle
    val Stress: ImageVector by lazy { lineIcon("Stress", "M3.5 12h3.4l2.3-6 3.8 12 2.6-7.5 1.4 1.5h3.5") }
    val Sleep: ImageVector by lazy { lineIcon("Sleep", "M19.5 14.2A8 8 0 0 1 9.8 4.5a8 8 0 1 0 9.7 9.7z") }
    val Diet: ImageVector by lazy { lineIcon("Diet", "M4.5 19.5c0-8.8 5.9-14.7 15-15-0.3 9.1-6.2 15-15 15z", "M4.5 19.5L14 10") }
    val Sun: ImageVector by lazy {
        lineIcon(
            "Sun",
            circle(12f, 12f, 4f),
            "M12 2.5v2.5", "M12 19v2.5", "M2.5 12H5", "M19 12h2.5",
            "M5.3 5.3l1.8 1.8", "M16.9 16.9l1.8 1.8", "M5.3 18.7l1.8-1.8", "M16.9 7.1l1.8-1.8",
        )
    }
    val Work: ImageVector by lazy {
        lineIcon(
            "Work",
            "M4 8.5A1.5 1.5 0 0 1 5.5 7h13A1.5 1.5 0 0 1 20 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5z",
            "M9 7V5.5A1.5 1.5 0 0 1 10.5 4h3A1.5 1.5 0 0 1 15 5.5V7",
            "M4 12.5h16",
        )
    }
    val Hormonal: ImageVector by lazy { lineIcon("Hormonal", "M3 12c1.5-4 3-4 4.5 0s3 4 4.5 0 3-4 4.5 0 3 4 4.5 0") }
    val Products: ImageVector by lazy {
        lineIcon(
            "Products",
            "M9.5 3.5h5v3h-5z",
            "M8 6.5h8l1 3v9.5a1.5 1.5 0 0 1-1.5 1.5h-7A1.5 1.5 0 0 1 7 19V9.5z",
            "M7 13h10",
        )
    }
    val Climate: ImageVector by lazy { lineIcon("Climate", "M7.5 18.5a4 4 0 0 1-0.6-7.95A6 6 0 0 1 18.5 9.5a4.5 4.5 0 0 1-0.5 9z") }
    val Illness: ImageVector by lazy { lineIcon("Illness", circle(12f, 12f, 8.5f), "M12 8.5v7", "M8.5 12h7") }

    // Bottom navigation
    val Home: ImageVector by lazy {
        lineIcon("Home", "M4.5 10.5L12 4l7.5 6.5V19a1 1 0 0 1-1 1h-13a1 1 0 0 1-1-1z", "M9.5 20v-6h5v6")
    }
    val SkinPrint: ImageVector by lazy {
        lineIcon("SkinPrint", circle(12f, 12f, 8.5f), "M12 7a5 5 0 0 1 5 5", "M12 17a5 5 0 0 1-5-5", circle(12f, 12f, 1.6f))
    }
    val Scan: ImageVector by lazy {
        lineIcon(
            "Scan",
            "M4 8.5v-3A1.5 1.5 0 0 1 5.5 4h3",
            "M15.5 4h3A1.5 1.5 0 0 1 20 5.5v3",
            "M20 15.5v3a1.5 1.5 0 0 1-1.5 1.5h-3",
            "M8.5 20h-3A1.5 1.5 0 0 1 4 18.5v-3",
            "M7 12h10",
        )
    }
    val Learn: ImageVector by lazy {
        lineIcon("Learn", "M4.5 5.5A2 2 0 0 1 6.5 3.5h13v14h-13a2 2 0 0 0-2 2z", "M4.5 19.5a2 2 0 0 1 2-2h13")
    }
    val Community: ImageVector by lazy {
        lineIcon(
            "Community",
            circle(9f, 8.5f, 3f),
            "M3.5 19.5c0-3.3 2.5-5.5 5.5-5.5s5.5 2.2 5.5 5.5",
            "M15.5 5.7a3 3 0 0 1 0 5.6",
            "M16.5 14.2c2.4 0.5 4 2.5 4 5.3",
        )
    }

    // Metrics
    val Droplet: ImageVector by lazy { lineIcon("Droplet", "M12 3.5s6 6.3 6 10.5a6 6 0 0 1-12 0c0-4.2 6-10.5 6-10.5z") }

    /** SVG path for a full circle, built from two arcs. */
    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun lineIcon(
        name: String,
        vararg strokes: String,
        strokeWidth: Float = 1.5f,
        fills: List<String> = emptyList(),
    ): ImageVector = ImageVector.Builder(
        name = "Skinthesia.$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        strokes.forEach { pathData ->
            addPath(
                pathData = addPathNodes(pathData),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        fills.forEach { pathData ->
            addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
        }
    }.build()
}
