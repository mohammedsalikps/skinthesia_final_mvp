package com.skinthesia.feature.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.AdaptivePlanRoute
import com.skinthesia.core.navigation.CheckInIntroRoute
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.ProgressComparisonRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.components.ComparisonBars
import com.skinthesia.core.ui.components.DeltaBadge
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.SourceBadge
import com.skinthesia.core.ui.components.fmt
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.MetricKind
import com.skinthesia.domain.model.ProgressComparison
import com.skinthesia.domain.model.ProgressMetric
import com.skinthesia.domain.model.ProgressSnapshot
import com.skinthesia.domain.usecase.CompleteAssessmentUseCase
import com.skinthesia.domain.usecase.ProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ProgressUiState(
    val loading: Boolean = true,
    val comparison: ProgressComparison? = null,
    val finishing: Boolean = false,
)

class ProgressComparisonViewModel(
    val assessmentId: String?,
    val flow: FlowKind,
    private val progress: ProgressUseCase,
    private val completeAssessment: CompleteAssessmentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val comparison = assessmentId?.let { progress.comparisonFor(it) } ?: progress.latestComparison()
            _state.value = ProgressUiState(loading = false, comparison = comparison)
        }
    }

    /** Completes the check-in, which adapts the plan, then moves on. */
    fun finish(onDone: () -> Unit) {
        if (_state.value.finishing) return
        _state.update { it.copy(finishing = true) }
        viewModelScope.launch {
            assessmentId?.let { completeAssessment(it) }
            _state.update { it.copy(finishing = false) }
            onDone()
        }
    }
}

/** Screen 30: Day 1 against the latest check-in, photo by photo and metric by metric. */
@Composable
fun ProgressComparisonScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<ProgressComparisonRoute>()
        ProgressComparisonViewModel(route.assessmentId, route.flow.toFlowKind(), progress, completeAssessment)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val checkIn = viewModel.flow == FlowKind.CHECK_IN

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Your progress", onBack = navigator::back) },
        bottomBar = if (checkIn && state.comparison != null) {
            {
                SkinthesiaPrimaryButton(
                    text = "See My Updated Plan",
                    loading = state.finishing,
                    enabled = !state.finishing,
                    onClick = { viewModel.finish { navigator.replace(AdaptivePlanRoute(FlowKind.CHECK_IN.name)) } },
                )
            }
        } else {
            null
        },
    ) {
        val comparison = state.comparison
        when {
            state.loading -> LoadingState(message = "Comparing your results")
            comparison == null -> EmptyState(
                icon = SkinthesiaIcons.Trend,
                title = "No check-ins yet",
                body = "Complete a weekly check-in to compare with Day 1.",
                actionLabel = "Start a check-in",
                onAction = { navigator.navigate(CheckInIntroRoute) },
            )
            else -> ComparisonContent(comparison)
        }
    }
}

@Composable
private fun ComparisonContent(comparison: ProgressComparison) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val from = comparison.baseline
    val to = comparison.current

    FadeInUp {
        Column {
            SectionOverline(text = "${from.weekLabel} → ${to.weekLabel}")
            Spacer(Modifier.height(8.dp))
            Text(text = comparison.headline, style = typography.display, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(text = comparison.summary, style = typography.body, color = colors.textSecondary)
        }
    }
    Spacer(Modifier.height(spacing.lg))
    FadeInUp(delayMillis = motion.stagger(1)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PhotoPanel(snapshot = from, modifier = Modifier.weight(1f))
            PhotoPanel(snapshot = to, modifier = Modifier.weight(1f), emphasized = true)
        }
    }
    Spacer(Modifier.height(10.dp))
    Text(
        text = "Photos are private to this device. Differences in light and angle affect how skin looks.",
        style = typography.caption,
        color = colors.textMuted,
    )

    val overall = comparison.metrics.firstOrNull { it.kind == MetricKind.SKINPRINT }
    val dimensions = comparison.metrics.filter { it.kind == MetricKind.DIMENSION }
    val sensors = comparison.metrics.filter { it.kind == MetricKind.SENSOR }

    overall?.let { metric ->
        Spacer(Modifier.height(spacing.lg))
        SkinthesiaCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    SectionOverline(text = "SkinPrint")
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = fmt(metric.from, 0), style = typography.metricLarge, color = colors.textSecondary)
                        Icon(SkinthesiaIcons.ArrowRight, contentDescription = "to", tint = colors.textMuted, modifier = Modifier.padding(horizontal = 10.dp).size(22.dp))
                        Text(text = fmt(metric.to, 0), style = typography.metricLarge, color = colors.textPrimary)
                    }
                }
                DeltaBadge(delta = metric.shownDelta(), improved = metric.shownImproved())
            }
            Spacer(Modifier.height(12.dp))
            ComparisonBars(from = metric.from.toFloat(), to = metric.to.toFloat(), fromLabel = from.weekLabel, toLabel = to.weekLabel)
        }
    }

    if (dimensions.isNotEmpty()) {
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "By dimension", overline = "Where it moved")
        Spacer(Modifier.height(12.dp))
        SkinthesiaCard {
            dimensions.forEachIndexed { index, metric ->
                MetricRow(metric, fromLabel = from.weekLabel, toLabel = to.weekLabel, bars = true)
                if (index < dimensions.lastIndex) SkinthesiaDivider()
            }
        }
    }

    if (sensors.isNotEmpty()) {
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "Probe readings", overline = "Averages across three areas")
        Spacer(Modifier.height(12.dp))
        SkinthesiaCard {
            sensors.forEachIndexed { index, metric ->
                MetricRow(metric, fromLabel = from.weekLabel, toLabel = to.weekLabel, bars = false)
                if (index < sensors.lastIndex) SkinthesiaDivider()
            }
        }
    }

    Spacer(Modifier.height(spacing.lg))
    InfoNotice(
        text = "Visual estimates shift with light and angle, and probe readings in this build are simulated. The trend across several check-ins matters more than any single change.",
    )
}

@Composable
private fun PhotoPanel(snapshot: ProgressSnapshot, modifier: Modifier, emphasized: Boolean = false) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(SkinthesiaTheme.shapes.card)
                .background(colors.surfaceMuted),
        ) {
            val path = snapshot.photoPath
            if (path != null) {
                SkinthesiaImage(source = ImageSource.LocalFile(path), contentDescription = "Selfie from ${snapshot.weekLabel}", modifier = Modifier.fillMaxSize(), maxDimension = 700)
            } else {
                FaceDiagram(contentDescription = "No photo for ${snapshot.weekLabel}", modifier = Modifier.fillMaxSize().padding(20.dp))
            }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(if (emphasized) colors.primary else colors.glass)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(text = snapshot.weekLabel, style = typography.labelSmall, color = if (emphasized) colors.textOnPrimary else colors.textPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "SkinPrint", style = typography.caption, color = colors.textMuted, modifier = Modifier.weight(1f))
            Text(text = snapshot.overall.toString(), style = typography.metric, color = if (emphasized) colors.primary else colors.textPrimary)
        }
    }
}

@Composable
private fun MetricRow(metric: ProgressMetric, fromLabel: String, toLabel: String, bars: Boolean) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val unit = if (metric.unit.isBlank()) "" else " " + metric.unit
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = metric.label, style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text(
                text = fmt(metric.from, metric.decimals) + " → " + fmt(metric.to, metric.decimals) + unit,
                style = typography.numeric,
                color = colors.textSecondary,
            )
            Spacer(Modifier.width(10.dp))
            DeltaBadge(delta = metric.shownDelta(), improved = metric.shownImproved(), decimals = metric.decimals)
        }
        if (bars) {
            Spacer(Modifier.height(10.dp))
            ComparisonBars(from = metric.from.toFloat(), to = metric.to.toFloat(), fromLabel = fromLabel, toLabel = toLabel, decimals = metric.decimals)
        } else {
            Spacer(Modifier.height(8.dp))
            SourceBadge(metric.source)
        }
    }
}

/** The change between the values as displayed, so 5.5 to 5.4 reads as -0.1 rather than -0.0. */
private fun ProgressMetric.shownDelta(): Double = fmt(to, decimals).toDouble() - fmt(from, decimals).toDouble()

private fun ProgressMetric.shownImproved(): Boolean? = if (abs(shownDelta()) < 1e-9) null else improved
