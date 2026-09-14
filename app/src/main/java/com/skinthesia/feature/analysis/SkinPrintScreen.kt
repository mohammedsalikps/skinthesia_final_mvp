package com.skinthesia.feature.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.ai.skinprint.SkinPrintCalculator
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.ReportRoute
import com.skinthesia.core.navigation.SkinPrintRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.DeltaBadge
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.KeyValueRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScoreRing
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.SourceBadge
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatDay
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.SkinScore
import com.skinthesia.domain.repository.AssessmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SkinPrintUiState(
    val loading: Boolean = true,
    val skinPrint: SkinPrint? = null,
    val weekLabel: String = "",
    val previousLabel: String? = null,
    val previous: SkinPrint? = null,
)

class SkinPrintViewModel(
    val assessmentId: String,
    val onboarding: Boolean,
    private val assessments: AssessmentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SkinPrintUiState())
    val state: StateFlow<SkinPrintUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val assessment = assessments.get(assessmentId)
            val previous = assessment?.let { current ->
                assessments.completedList()
                    .filter { it.id != current.id && it.week < current.week && it.skinPrint != null }
                    .maxByOrNull { it.week }
            }
            _state.value = SkinPrintUiState(
                loading = false,
                skinPrint = assessment?.skinPrint,
                weekLabel = assessment?.weekLabel.orEmpty(),
                previousLabel = previous?.weekLabel,
                previous = previous?.skinPrint,
            )
        }
    }
}

/** Screen 21: the SkinPrint composite, its five dimensions and exactly how it is calculated. */
@Composable
fun SkinPrintScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<SkinPrintRoute>()
        SkinPrintViewModel(route.assessmentId, route.onboarding, assessments)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val skinPrint = state.skinPrint

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "SkinPrint", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = if (viewModel.onboarding) "View My Skin Report" else "View Full Report",
                onClick = { navigator.navigate(ReportRoute(viewModel.assessmentId, viewModel.onboarding)) },
                enabled = skinPrint != null,
            )
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Loading your SkinPrint")
            skinPrint == null -> ScreenHeader(title = "No SkinPrint yet", subtitle = "Complete an analysis to see your SkinPrint.", centered = true)
            else -> SkinPrintContent(skinPrint, state)
        }
    }
}

@Composable
private fun SkinPrintContent(skinPrint: SkinPrint, state: SkinPrintUiState) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val previous = state.previous

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        SectionOverline(text = "Your SkinPrint")
        Spacer(Modifier.height(14.dp))
        ScoreRing(score = skinPrint.overall, size = 230.dp, label = skinPrint.band.label)
        Spacer(Modifier.height(14.dp))
        Text(text = "${state.weekLabel} · ${formatDay(skinPrint.createdAt)}", style = typography.caption, color = colors.textMuted)
        if (previous != null && state.previousLabel != null) {
            Spacer(Modifier.height(8.dp))
            val delta = skinPrint.overall - previous.overall
            DeltaBadge(
                delta = delta.toDouble(),
                improved = when {
                    delta > 0 -> true
                    delta < 0 -> false
                    else -> null
                },
                suffix = " since ${state.previousLabel}",
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "A tracking indicator for your own progress, not a medical score.",
            style = typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.height(spacing.lg))
    SectionHeader(title = "Five dimensions", overline = "What makes it up")
    Spacer(Modifier.height(12.dp))
    FadeInUp(delayMillis = motion.stagger(2)) {
        SkinthesiaCard {
            skinPrint.scores.forEachIndexed { index, score ->
                DimensionRow(
                    score = score,
                    weight = SkinPrintCalculator.WEIGHTS[score.dimension] ?: 0.0,
                    previous = previous?.score(score.dimension)?.value,
                    delayMillis = 400 + index * 120,
                )
                if (index < skinPrint.scores.lastIndex) SkinthesiaDivider()
            }
        }
    }
    Spacer(Modifier.height(spacing.md))
    HowCalculatedCard(skinPrint)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DimensionRow(score: SkinScore, weight: Double, previous: Int?, delayMillis: Int) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    var expanded by rememberSaveable(score.dimension.name) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SkinthesiaTheme.shapes.field)
            .clickable(onClickLabel = if (expanded) "Hide details" else "Show details") { expanded = !expanded }
            .padding(vertical = 12.dp)
            .animateContentSize(),
    ) {
        Row(Modifier.semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
            IconBadge(score.dimension.icon, size = 34.dp)
            Spacer(Modifier.width(12.dp))
            Text(text = score.dimension.label, style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            if (previous != null && previous != score.value) {
                DeltaBadge(delta = (score.value - previous).toDouble(), improved = score.value > previous)
                Spacer(Modifier.width(8.dp))
            }
            Text(text = score.value.toString(), style = typography.metric, color = colors.textPrimary)
            Spacer(Modifier.width(6.dp))
            Icon(
                if (expanded) SkinthesiaIcons.ChevronUp else SkinthesiaIcons.ChevronDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        LevelBar(fraction = score.value / 100f, color = colors.primary, delayMillis = delayMillis)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            score.sources.forEach { SourceBadge(it) }
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Text(text = score.explanation, style = typography.bodySmall, color = colors.textSecondary)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Weight in overall ${(weight * 100).roundToInt()}% · Confidence ${(score.confidence * 100).roundToInt()}%",
                style = typography.caption,
                color = colors.textMuted,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HowCalculatedCard(skinPrint: SkinPrint) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    var open by rememberSaveable { mutableStateOf(false) }
    val inputs = skinPrint.inputs
    SkinthesiaCard(onClick = { open = !open }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(SkinthesiaIcons.Info, size = 30.dp)
            Spacer(Modifier.width(12.dp))
            Text(text = "How SkinPrint is calculated", style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Icon(
                if (open) SkinthesiaIcons.ChevronUp else SkinthesiaIcons.ChevronDown,
                contentDescription = if (open) "Collapse" else "Expand",
                tint = colors.textMuted,
                modifier = Modifier.size(16.dp),
            )
        }
        AnimatedVisibility(visible = open) {
            Column {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Visual dimensions start at 100 and step down as related features look more visible in your photo. " +
                        "Hydration uses your probe's average hydration index, or an estimate from your skin type when there are no readings. " +
                        "Your overall SkinPrint is a weighted average:",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(10.dp))
                SkinPrintCalculator.WEIGHTS.forEach { (dimension, weight) ->
                    KeyValueRow(label = dimension.label, value = "${(weight * 100).roundToInt()}%")
                }
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (inputs.hasCamera) Tag(text = if (inputs.cameraSimulated) "Photo · development model" else "Photo", tone = TagTone.MIST)
                    if (inputs.hasSensors) Tag(text = if (inputs.sensorsSimulated) "Probe · simulated" else "Probe", tone = if (inputs.sensorsSimulated) TagTone.GOLD else TagTone.SAGE)
                    if (inputs.hasSelfReport) Tag(text = "Self-report", tone = TagTone.NEUTRAL)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = "Algorithm ${skinPrint.algorithmVersion}", style = typography.caption, color = colors.textMuted)
            }
        }
    }
}
