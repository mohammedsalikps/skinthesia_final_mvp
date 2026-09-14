package com.skinthesia.feature.measurement

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.components.fmt
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.SensorType

val SensorType.icon: ImageVector
    get() = when (this) {
        SensorType.PH -> SkinthesiaIcons.Balance
        SensorType.HYDRATION -> SkinthesiaIcons.Droplet
        SensorType.TEMPERATURE -> SkinthesiaIcons.Thermometer
    }

fun SensorType.format(value: Double): String = when (this) {
    SensorType.PH -> fmt(value, 1)
    SensorType.HYDRATION -> fmt(value, 0)
    SensorType.TEMPERATURE -> fmt(value, 1) + "°C"
}

val SensorType.caption: String
    get() = when (this) {
        SensorType.PH -> "Surface pH"
        SensorType.HYDRATION -> "Hydration index"
        SensorType.TEMPERATURE -> "Skin temperature"
    }

/** Four quiet bars for received signal strength. */
@Composable
fun SignalBars(dbm: Int?, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    val level = when {
        dbm == null -> 0
        dbm >= -55 -> 4
        dbm >= -65 -> 3
        dbm >= -75 -> 2
        else -> 1
    }
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = "Signal $level of 4" },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        (1..4).forEach { bar ->
            Box(
                Modifier
                    .width(3.dp)
                    .height((4 + bar * 3).dp)
                    .clip(CircleShape)
                    .background(if (bar <= level) colors.textSecondary else colors.border),
            )
        }
    }
}

/** Compact battery gauge with the percentage. */
@Composable
fun BatteryMeter(percent: Int?, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    val value = (percent ?: 0).coerceIn(0, 100)
    Row(modifier = modifier.clearAndSetSemantics { contentDescription = "Battery $value percent" }, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(24.dp)
                .height(12.dp)
                .clip(SkinthesiaTheme.shapes.extraSmall)
                .border(1.dp, colors.textSecondary, SkinthesiaTheme.shapes.extraSmall)
                .padding(2.dp),
        ) {
            Box(Modifier.fillMaxWidth(value / 100f).height(8.dp).clip(SkinthesiaTheme.shapes.extraSmall).background(if (value > 20) colors.success else colors.warning))
        }
        Spacer(Modifier.width(6.dp))
        Text(text = "$value%", style = SkinthesiaTheme.typography.numeric, color = colors.textSecondary)
    }
}

/** The honest label shown wherever simulated probe data appears. */
@Composable
fun SimulatedNote(modifier: Modifier = Modifier, text: String = "Development mode: this probe and its readings are simulated until Skinthesia hardware is connected.") {
    val colors = SkinthesiaTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SkinthesiaTheme.shapes.field)
            .background(colors.goldSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(SkinthesiaIcons.Info, contentDescription = null, tint = colors.goldStrong, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(text = text, style = SkinthesiaTheme.typography.bodySmall, color = colors.goldStrong)
    }
}

/** One live or final sensor value. */
@Composable
fun SensorTile(sensor: SensorType, value: Double?, modifier: Modifier = Modifier, settled: Boolean = false) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(
        modifier = modifier
            .clip(SkinthesiaTheme.shapes.tile)
            .background(colors.surface)
            .border(1.dp, if (settled) colors.success.copy(alpha = 0.5f) else colors.border, SkinthesiaTheme.shapes.tile)
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .clearAndSetSemantics { contentDescription = sensor.caption + " " + (value?.let { sensor.format(it) } ?: "waiting") },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(sensor.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(8.dp))
        Text(text = value?.let { sensor.format(it) } ?: "—", style = typography.metric, color = colors.textPrimary)
        Spacer(Modifier.height(2.dp))
        Text(text = sensor.label, style = typography.caption, color = colors.textMuted)
    }
}

/** A small breathing dot for work in progress; steady when reduced motion is on. */
@Composable
fun PulseDot(modifier: Modifier = Modifier, color: Color = SkinthesiaTheme.colors.primary, size: Dp = 8.dp) {
    val reduced = SkinthesiaTheme.motion.reducedMotion
    val transition = rememberInfiniteTransition(label = "pulseDot")
    val alpha by transition.animateFloat(0.25f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulseAlpha")
    Box(modifier.size(size).clip(CircleShape).background(color.copy(alpha = if (reduced) 1f else alpha)))
}

@Composable
fun SimulatedTag(modifier: Modifier = Modifier) = Tag(text = "Simulated", tone = TagTone.GOLD, dot = true, modifier = modifier)
