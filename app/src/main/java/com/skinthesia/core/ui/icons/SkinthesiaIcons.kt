package com.skinthesia.core.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Skinthesia's own thin-line icon set, drawn on a 24-unit grid with 1.5 strokes and
 * round joins so every glyph shares the calm, editorial line of the reference.
 * Icons are single-colour and take the tint passed to [androidx.compose.material3.Icon].
 */
object SkinthesiaIcons {

    // Navigation and actions
    val ArrowRight: ImageVector by lazy { line("ArrowRight", "M5 12h14", "M13 6l6 6-6 6") }
    val ArrowLeft: ImageVector by lazy { line("ArrowLeft", "M19 12H5", "M11 6l-6 6 6 6") }
    val ArrowUpRight: ImageVector by lazy { line("ArrowUpRight", "M7 17L17 7", "M9 7h8v8") }
    val Check: ImageVector by lazy { line("Check", "M5 12.5l4.5 4.5L19 7", width = 1.8f) }
    val ChevronDown: ImageVector by lazy { line("ChevronDown", "M6 9.5l6 6 6-6") }
    val ChevronUp: ImageVector by lazy { line("ChevronUp", "M6 14.5l6-6 6 6") }
    val ChevronRight: ImageVector by lazy { line("ChevronRight", "M9.5 6l6 6-6 6") }
    val Close: ImageVector by lazy { line("Close", "M6 6l12 12", "M18 6L6 18") }
    val Plus: ImageVector by lazy { line("Plus", "M12 5v14", "M5 12h14") }
    val Minus: ImageVector by lazy { line("Minus", "M5 12h14") }
    val Menu: ImageVector by lazy { line("Menu", "M4 7h16", "M4 12h16", "M4 17h16") }
    val More: ImageVector by lazy { line("More", fills = listOf(circle(6f, 12f, 1.3f), circle(12f, 12f, 1.3f), circle(18f, 12f, 1.3f))) }
    val Search: ImageVector by lazy { line("Search", circle(10.5f, 10.5f, 6.5f), "M15.5 15.5L20 20") }
    val Filter: ImageVector by lazy {
        line("Filter", "M4 7h9", "M17 7h3", circle(15f, 7f, 2f), "M4 17h3", "M11 17h9", circle(9f, 17f, 2f))
    }
    val Edit: ImageVector by lazy {
        line("Edit", "M4 20h4l10.5-10.5a1.5 1.5 0 0 0 0-2.1l-1.9-1.9a1.5 1.5 0 0 0-2.1 0L4 16z", "M13.5 6.5l4 4")
    }
    val Trash: ImageVector by lazy {
        line(
            "Trash",
            "M4.5 7h15",
            "M9.5 7V5a1 1 0 0 1 1-1h3a1 1 0 0 1 1 1v2",
            "M6.5 7l1 12a1.5 1.5 0 0 0 1.5 1.4h6a1.5 1.5 0 0 0 1.5-1.4l1-12",
            "M10 11v6",
            "M14 11v6",
        )
    }
    val Refresh: ImageVector by lazy { line("Refresh", "M19 12a7 7 0 1 1-2-4.9", "M19.5 4v4h-4") }
    val Export: ImageVector by lazy {
        line("Export", "M12 14V3.5", "M8 7.5l4-4 4 4", "M5 12.5V19a1.5 1.5 0 0 0 1.5 1.5h11A1.5 1.5 0 0 0 19 19v-6.5")
    }
    val Logout: ImageVector by lazy {
        line("Logout", "M14 4.5h4a1.5 1.5 0 0 1 1.5 1.5v12a1.5 1.5 0 0 1-1.5 1.5h-4", "M10 16l4-4-4-4", "M14 12H4.5")
    }
    val Info: ImageVector by lazy { line("Info", circle(12f, 12f, 8.5f), "M12 11v5", fills = listOf(circle(12f, 8f, 0.9f))) }
    val Alert: ImageVector by lazy { line("Alert", "M12 4l9 15.5H3z", "M12 10v4.5", fills = listOf(circle(12f, 17.2f, 0.9f))) }
    val Bell: ImageVector by lazy {
        line("Bell", "M6.5 16.5v-5.5a5.5 5.5 0 0 1 11 0v5.5l1.5 2h-14z", "M10 20.5a2 2 0 0 0 4 0")
    }
    val Heart: ImageVector by lazy { line("Heart", HEART) }
    val HeartFilled: ImageVector by lazy { solid("HeartFilled", HEART) }
    val Comment: ImageVector by lazy {
        line("Comment", "M5 5.5h14a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1-1.5 1.5H10l-4.5 3.5V16.5H5A1.5 1.5 0 0 1 3.5 15V7A1.5 1.5 0 0 1 5 5.5z")
    }
    val Bookmark: ImageVector by lazy { line("Bookmark", BOOKMARK) }
    val BookmarkFilled: ImageVector by lazy { solid("BookmarkFilled", BOOKMARK) }
    val Star: ImageVector by lazy { line("Star", STAR, width = 1.3f) }
    val StarFilled: ImageVector by lazy { solid("StarFilled", STAR) }

    // Camera
    val Camera: ImageVector by lazy {
        line(
            "Camera",
            "M4 8.5A1.5 1.5 0 0 1 5.5 7h2.2l1.4-2h5.8l1.4 2h2.2A1.5 1.5 0 0 1 20 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5z",
            circle(12f, 12.5f, 3.2f),
        )
    }
    val Retake: ImageVector by lazy { line("Retake", "M5.6 8A7.5 7.5 0 1 1 4.5 12", "M4.5 4.5V8H8") }
    val Flip: ImageVector by lazy {
        line("Flip", "M4 12a8 8 0 0 1 13.7-5.6", "M20 12a8 8 0 0 1-13.7 5.6", "M17.7 2.4v4h-4", "M6.3 21.6v-4h4")
    }
    val Gallery: ImageVector by lazy {
        line(
            "Gallery",
            "M4 5.5A1.5 1.5 0 0 1 5.5 4h13A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5z",
            "M4 16l4.5-4.5 4 4 3-3L20 17",
            fills = listOf(circle(15.5f, 9f, 1.3f)),
        )
    }
    val Face: ImageVector by lazy {
        line(
            "Face",
            "M12 3.5c-4 0-6.5 3.2-6.5 7.3 0 4.8 3 9.7 6.5 9.7s6.5-4.9 6.5-9.7c0-4.1-2.5-7.3-6.5-7.3z",
            "M9.6 15.3c1.4 1 3.4 1 4.8 0",
            fills = listOf(circle(9.6f, 11f, 0.8f), circle(14.4f, 11f, 0.8f)),
        )
    }
    val Eye: ImageVector by lazy {
        line("Eye", "M2.5 12s3.5-6.5 9.5-6.5 9.5 6.5 9.5 6.5-3.5 6.5-9.5 6.5S2.5 12 2.5 12z", circle(12f, 12f, 2.8f))
    }

    // Goals and concerns
    val Sparkle: ImageVector by lazy {
        line(
            "Sparkle",
            "M12 3.5l1.9 5.6 5.6 1.9-5.6 1.9L12 18.5l-1.9-5.6L4.5 11l5.6-1.9z",
            "M18.5 16.5l0.7 2 2 0.7-2 0.7-0.7 2-0.7-2-2-0.7 2-0.7z",
            width = 1.3f,
        )
    }
    val Breakouts: ImageVector by lazy { line("Breakouts", circle(8f, 10.5f, 2.2f), circle(15.5f, 9.5f, 2.6f), circle(12f, 16.5f, 2f)) }
    val EvenTone: ImageVector by lazy {
        line("EvenTone", "M5 5h5.5v5.5H5z", "M13.5 5H19v5.5h-5.5z", "M5 13.5h5.5V19H5z", "M13.5 13.5H19V19h-5.5z", width = 1.3f)
    }
    val Texture: ImageVector by lazy { line("Texture", "M3.5 8.5c2.8-3 5.7 3 8.5 0s5.7-3 8.5 0", "M3.5 15.5c2.8-3 5.7 3 8.5 0s5.7-3 8.5 0") }
    val DarkSpots: ImageVector by lazy {
        line("DarkSpots", circle(12f, 12f, 8.5f), fills = listOf(circle(9f, 10f, 1.3f), circle(14.5f, 12.5f, 1.1f), circle(11f, 15.3f, 0.9f)))
    }
    val Pores: ImageVector by lazy {
        line(
            "Pores",
            circle(12f, 12f, 8.5f),
            fills = listOf(
                circle(8.5f, 9f, 0.7f), circle(12f, 8f, 0.7f), circle(15.5f, 9f, 0.7f),
                circle(9.5f, 12.5f, 0.7f), circle(14.5f, 12.5f, 0.7f), circle(12f, 15.5f, 0.7f),
            ),
        )
    }
    val Glow: ImageVector by lazy {
        line(
            "Glow",
            circle(12f, 12f, 3.5f),
            "M12 3.5v2", "M12 18.5v2", "M3.5 12h2", "M18.5 12h2",
            "M6 6l1.4 1.4", "M16.6 16.6L18 18", "M6 18l1.4-1.4", "M16.6 7.4L18 6",
        )
    }
    val Shield: ImageVector by lazy {
        line("Shield", "M12 3.5l7 2.6v5.4c0 4.6-3 8-7 9.5-4-1.5-7-4.9-7-9.5V6.1z", "M9.5 12.2l1.8 1.8 3.4-3.6")
    }
    val Calm: ImageVector by lazy {
        line("Calm", "M12 4c2.3 3 6 5.6 6 10a6 6 0 0 1-12 0c0-2.2 1-3.6 2.2-4.8 0.4 1.2 1.1 2 2 2.4C10 8.8 10.8 6.4 12 4z")
    }
    val FineLines: ImageVector by lazy {
        line("FineLines", "M5 8.5c4.5-2 9.5-2 14 0", "M6.5 12.5c3.7-1.5 7.3-1.5 11 0", "M8 16.5c2.7-1 5.3-1 8 0")
    }
    val UnderEye: ImageVector by lazy {
        line("UnderEye", "M3.5 11s3.5-4.5 8.5-4.5 8.5 4.5 8.5 4.5-3.5 4.5-8.5 4.5S3.5 11 3.5 11z", circle(12f, 11f, 2f), "M6 18.5c3.8 1.4 8.2 1.4 12 0")
    }
    val Droplet: ImageVector by lazy { line("Droplet", "M12 3.5s6 6.3 6 10.5a6 6 0 0 1-12 0c0-4.2 6-10.5 6-10.5z") }
    val Target: ImageVector by lazy { line("Target", circle(12f, 12f, 8.5f), circle(12f, 12f, 5f), fills = listOf(circle(12f, 12f, 1.5f))) }
    val Layers: ImageVector by lazy {
        line("Layers", "M12 4l8.5 4.5L12 13 3.5 8.5z", "M3.5 12.5L12 17l8.5-4.5", "M3.5 16.5L12 21l8.5-4.5")
    }

    // Lifestyle
    val Stress: ImageVector by lazy { line("Stress", "M3.5 12h3.4l2.3-6 3.8 12 2.6-7.5 1.4 1.5h3.5") }
    val Moon: ImageVector by lazy { line("Moon", "M19.5 14.2A8 8 0 0 1 9.8 4.5a8 8 0 1 0 9.7 9.7z") }
    val Sleep: ImageVector get() = Moon
    val Diet: ImageVector by lazy { line("Diet", "M4.5 19.5c0-8.8 5.9-14.7 15-15-0.3 9.1-6.2 15-15 15z", "M4.5 19.5L14 10") }
    val Sun: ImageVector by lazy {
        line(
            "Sun",
            circle(12f, 12f, 4f),
            "M12 2.5v2.5", "M12 19v2.5", "M2.5 12H5", "M19 12h2.5",
            "M5.3 5.3l1.8 1.8", "M16.9 16.9l1.8 1.8", "M5.3 18.7l1.8-1.8", "M16.9 7.1l1.8-1.8",
        )
    }
    val Sunrise: ImageVector by lazy {
        line("Sunrise", "M3.5 17.5h17", "M7 17.5a5 5 0 0 1 10 0", "M12 5.5v3", "M5.4 10.4l1.8 1.3", "M18.6 10.4l-1.8 1.3", "M8.5 20.5h7")
    }
    val Work: ImageVector by lazy {
        line(
            "Work",
            "M4 8.5A1.5 1.5 0 0 1 5.5 7h13A1.5 1.5 0 0 1 20 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5z",
            "M9 7V5.5A1.5 1.5 0 0 1 10.5 4h3A1.5 1.5 0 0 1 15 5.5V7",
            "M4 12.5h16",
        )
    }
    val Movement: ImageVector by lazy {
        line("Movement", circle(14.5f, 4.8f, 1.6f), "M9 20l2.5-5 3 2V21", "M7 11.5l3-3.5 4 1 2 3 2.5 1", "M11.5 15l1-4.5")
    }
    val Products: ImageVector by lazy {
        line("Products", "M9.5 3.5h5v3h-5z", "M8 6.5h8l1 3v9.5a1.5 1.5 0 0 1-1.5 1.5h-7A1.5 1.5 0 0 1 7 19V9.5z", "M7 13h10")
    }
    val Climate: ImageVector by lazy { line("Climate", "M7.5 18.5a4 4 0 0 1-0.6-7.95A6 6 0 0 1 18.5 9.5a4.5 4.5 0 0 1-0.5 9z") }
    val Screen: ImageVector by lazy {
        line("Screen", "M4.5 5.5h15a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1h-15a1 1 0 0 1-1-1v-9a1 1 0 0 1 1-1z", "M9 20.5h6", "M12 16.5v4")
    }
    val Leaf: ImageVector get() = Diet

    // Probe and science
    val Probe: ImageVector by lazy {
        line(
            "Probe",
            "M10 3.5h4a1 1 0 0 1 1 1V9l-1 11.2a1.5 1.5 0 0 1-1.5 1.3h-1a1.5 1.5 0 0 1-1.5-1.3L9 9V4.5a1 1 0 0 1 1-1z",
            "M9.2 9h5.6",
            fills = listOf(circle(12f, 6.3f, 0.9f)),
        )
    }
    val Bluetooth: ImageVector by lazy { line("Bluetooth", "M7.5 7.5l9 9L12 21V3l4.5 4.5-9 9") }
    val Battery: ImageVector by lazy {
        line(
            "Battery",
            "M3.5 8.5A1.5 1.5 0 0 1 5 7h11.5A1.5 1.5 0 0 1 18 8.5v7a1.5 1.5 0 0 1-1.5 1.5H5a1.5 1.5 0 0 1-1.5-1.5z",
            "M20.5 10.5v3",
            "M6.5 10v4", "M9.5 10v4", "M12.5 10v4",
        )
    }
    val Signal: ImageVector by lazy { line("Signal", "M5 19v-3", "M9.5 19v-6", "M14 19v-9", "M18.5 19V7") }
    val Thermometer: ImageVector by lazy { line("Thermometer", "M10 5a2 2 0 0 1 4 0v9.3a3.8 3.8 0 1 1-4 0z", "M12 9v7") }
    val Balance: ImageVector by lazy {
        line("Balance", "M12 4v16", "M7 20h10", "M5 7.5h14", "M5 7.5l-2.5 6a2.5 2.5 0 0 0 5 0z", "M19 7.5l-2.5 6a2.5 2.5 0 0 0 5 0z")
    }
    val Flask: ImageVector by lazy {
        line(
            "Flask",
            "M9.5 3.5h5",
            "M10.5 3.5v5.2L5.6 17.6A1.9 1.9 0 0 0 7.3 20.5h9.4a1.9 1.9 0 0 0 1.7-2.9L13.5 8.7V3.5",
            "M7.8 14h8.4",
        )
    }
    val Trend: ImageVector by lazy { line("Trend", "M4 17l5-5 3.5 3.5L20 8", "M15 8h5v5") }
    val TrendDown: ImageVector by lazy { line("TrendDown", "M4 7l5 5 3.5-3.5L20 16", "M15 16h5v-5") }
    val Lightbulb: ImageVector by lazy {
        line(
            "Lightbulb",
            "M9 17.5h6",
            "M10 20.5h4",
            "M12 3.5a5.5 5.5 0 0 0-3.2 10c0.7 0.5 1.2 1.3 1.2 2.2v1.8h4v-1.8c0-0.9 0.5-1.7 1.2-2.2A5.5 5.5 0 0 0 12 3.5z",
        )
    }

    // Commerce and consultation
    val Bag: ImageVector by lazy {
        line("Bag", "M5.5 8h13l-1 11.5a1.5 1.5 0 0 1-1.5 1.3H8a1.5 1.5 0 0 1-1.5-1.3z", "M9 8V6.5a3 3 0 0 1 6 0V8")
    }
    val Truck: ImageVector by lazy {
        line("Truck", "M3.5 7h11v9.5h-11z", "M14.5 10h3.5l2.5 3v3.5h-6", circle(7.5f, 17.5f, 1.7f), circle(17f, 17.5f, 1.7f))
    }
    val Receipt: ImageVector by lazy {
        line("Receipt", "M6 3.5h12v17l-2-1.4-2 1.4-2-1.4-2 1.4-2-1.4-2 1.4z", "M9 8h6", "M9 11.5h6", "M9 15h4")
    }
    val Calendar: ImageVector by lazy {
        line(
            "Calendar",
            "M5 6.5h14a1 1 0 0 1 1 1V19a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7.5a1 1 0 0 1 1-1z",
            "M4 10.5h16",
            "M8.5 4.5v3.5",
            "M15.5 4.5v3.5",
        )
    }
    val Clock: ImageVector by lazy { line("Clock", circle(12f, 12f, 8.5f), "M12 7.5V12l3 2") }
    val Video: ImageVector by lazy {
        line("Video", "M4 7.5A1.5 1.5 0 0 1 5.5 6h9A1.5 1.5 0 0 1 16 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 4 16.5z", "M16 10.5l4-2.5v8l-4-2.5")
    }
    val Chat: ImageVector by lazy { line("Chat", "M4.5 12a7.5 7 0 1 1 3.4 5.9L4 19l1.1-3.6A6.8 6.8 0 0 1 4.5 12z") }
    val Award: ImageVector by lazy { line("Award", circle(12f, 9f, 5f), "M9 13.3L7.5 20.5l4.5-2.3 4.5 2.3-1.5-7.2") }
    val Globe: ImageVector by lazy {
        line(
            "Globe",
            circle(12f, 12f, 8.5f),
            "M3.5 12h17",
            "M12 3.5c2.5 2.4 3.6 5.2 3.6 8.5s-1.1 6.1-3.6 8.5c-2.5-2.4-3.6-5.2-3.6-8.5s1.1-6.1 3.6-8.5z",
        )
    }
    val Pin: ImageVector by lazy { line("Pin", "M12 21s-6.5-6-6.5-11a6.5 6.5 0 0 1 13 0c0 5-6.5 11-6.5 11z", circle(12f, 10f, 2.3f)) }

    // Account and privacy
    val User: ImageVector by lazy { line("User", circle(12f, 8.5f, 3.5f), "M5 20c0-3.9 3.1-6.5 7-6.5s7 2.6 7 6.5") }
    val Settings: ImageVector by lazy {
        line(
            "Settings",
            circle(12f, 12f, 3f),
            circle(12f, 12f, 6.8f),
            "M12 2.8v2.4", "M12 18.8v2.4", "M2.8 12h2.4", "M18.8 12h2.4",
            width = 1.4f,
        )
    }
    val Lock: ImageVector by lazy {
        line(
            "Lock",
            "M6.5 11h11a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1h-11a1 1 0 0 1-1-1v-7a1 1 0 0 1 1-1z",
            "M8.5 11V8a3.5 3.5 0 0 1 7 0v3",
            "M12 15v2",
        )
    }
    val Phone: ImageVector by lazy { line("Phone", "M8 3.5h8a1 1 0 0 1 1 1v15a1 1 0 0 1-1 1H8a1 1 0 0 1-1-1v-15a1 1 0 0 1 1-1z", "M11 17.5h2") }

    // Bottom navigation
    val Home: ImageVector by lazy { line("Home", "M4.5 10.5L12 4l7.5 6.5V19a1 1 0 0 1-1 1h-13a1 1 0 0 1-1-1z", "M9.5 20v-6h5v6") }
    val Journey: ImageVector by lazy {
        line("Journey", circle(6f, 18f, 2f), circle(18f, 6f, 2f), "M8 18h6.5a3.5 3.5 0 0 0 0-7h-5a3.5 3.5 0 0 1 0-7H16")
    }
    val Scan: ImageVector by lazy {
        line(
            "Scan",
            "M4 8.5v-3A1.5 1.5 0 0 1 5.5 4h3",
            "M15.5 4h3A1.5 1.5 0 0 1 20 5.5v3",
            "M20 15.5v3a1.5 1.5 0 0 1-1.5 1.5h-3",
            "M8.5 20h-3A1.5 1.5 0 0 1 4 18.5v-3",
            "M7 12h10",
        )
    }
    val Learn: ImageVector by lazy {
        line("Learn", "M4.5 5.5c2.5-1 5.2-1 7.5 0.8 2.3-1.8 5-1.8 7.5-0.8V19c-2.5-1-5.2-1-7.5 0.8-2.3-1.8-5-1.8-7.5-0.8z", "M12 6.3v13.5")
    }
    val Community: ImageVector by lazy {
        line(
            "Community",
            circle(9f, 8.5f, 3f),
            "M3.5 19.5c0-3.3 2.5-5.5 5.5-5.5s5.5 2.2 5.5 5.5",
            "M15.5 5.7a3 3 0 0 1 0 5.6",
            "M16.5 14.2c2.4 0.5 4 2.5 4 5.3",
        )
    }
    val SkinPrint: ImageVector by lazy {
        line("SkinPrint", circle(12f, 12f, 8.5f), "M12 7a5 5 0 0 1 5 5", "M12 17a5 5 0 0 1-5-5", fills = listOf(circle(12f, 12f, 1.6f)))
    }

    private const val HEART = "M12 19.5s-7.5-4.4-7.5-10A4.2 4.2 0 0 1 12 7.1a4.2 4.2 0 0 1 7.5 2.4c0 5.6-7.5 10-7.5 10z"
    private const val BOOKMARK = "M6.5 4.5h11v15.5L12 16.2 6.5 20z"
    private const val STAR = "M12 3.8l2.5 5.1 5.6 0.8-4 3.9 0.9 5.6-5-2.6-5 2.6 0.9-5.6-4-3.9 5.6-0.8z"

    /** SVG path for a full circle, built from two arcs. */
    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun line(
        name: String,
        vararg strokes: String,
        width: Float = 1.5f,
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
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        fills.forEach { pathData -> addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black)) }
    }.build()

    private fun solid(name: String, path: String): ImageVector = ImageVector.Builder(
        name = "Skinthesia.$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = addPathNodes(path),
            fill = SolidColor(Color.Black),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()
}
