package com.skinthesia.feature.report

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.ai.analysis.ReportBuilder
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.AreaDetailRoute
import com.skinthesia.core.navigation.AreasRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.PlanRoute
import com.skinthesia.core.navigation.ReportRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MiniRing
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaSecondaryButton
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.SourceBadge
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatDay
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.SkinReport
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.CompleteAssessmentUseCase
import com.skinthesia.feature.analysis.LevelBar
import com.skinthesia.feature.analysis.icon
import com.skinthesia.feature.analysis.tagTone
import com.skinthesia.feature.analysis.visibilityColor
import com.skinthesia.feature.measurement.SimulatedNote
import com.skinthesia.feature.measurement.caption
import com.skinthesia.feature.measurement.format
import com.skinthesia.feature.measurement.icon
import com.skinthesia.feature.plan.FocusAreaRow
import com.skinthesia.feature.plan.InsightCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportUiState(
    val loading: Boolean = true,
    val report: SkinReport? = null,
    val weekLabel: String = "",
    val building: Boolean = false,
)

class ReportViewModel(
    val assessmentId: String,
    val onboarding: Boolean,
    private val assessments: AssessmentRepository,
    private val builder: ReportBuilder,
    private val completeAssessment: CompleteAssessmentUseCase,
    private val profiles: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val assessment = assessments.get(assessmentId)
            _state.value = ReportUiState(loading = false, report = assessment?.let(builder::build), weekLabel = assessment?.weekLabel.orEmpty())
        }
    }

    /** Completes the baseline, which produces the first plan, and finishes onboarding. */
    fun buildPlan(onReady: () -> Unit) {
        if (_state.value.building) return
        _state.update { it.copy(building = true) }
        viewModelScope.launch {
            completeAssessment(assessmentId)
            if (onboarding) profiles.update { it.copy(onboardingStage = OnboardingStage.COMPLETE) }
            _state.update { it.copy(building = false) }
            onReady()
        }
    }
}

/** Screen 22: strengths, focus areas, measured values, visual analysis and insights. */
@Composable
fun ReportScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<ReportRoute>()
        ReportViewModel(route.assessmentId, route.onboarding, assessments, reportBuilder, completeAssessment, profiles)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val report = state.report

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Skin report", onBack = navigator::back) },
        bottomBar = {
            if (viewModel.onboarding) {
                SkinthesiaPrimaryButton(
                    text = "Build My Plan",
                    loading = state.building,
                    enabled = report != null && !state.building,
                    onClick = { viewModel.buildPlan { navigator.navigate(PlanRoute(onboarding = true)) } },
                )
            } else if (report != null) {
                SkinthesiaSecondaryButton(text = "See Areas for Improvement", onClick = { navigator.navigate(AreasRoute(viewModel.assessmentId)) })
            }
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Preparing your report")
            report == null -> ScreenHeader(title = "Report not available", subtitle = "Complete an analysis to see your skin report.", centered = true)
            else -> ReportContent(
                report = report,
                weekLabel = state.weekLabel,
                onArea = { area -> navigator.navigate(AreaDetailRoute(viewModel.assessmentId, area)) },
                onAllAreas = { navigator.navigate(AreasRoute(viewModel.assessmentId)) },
            )
        }
    }
}

private fun summaryOf(report: SkinReport): String {
    val best = report.skinPrint.scores.maxByOrNull { it.value }
    val focus = report.focusAreas.firstOrNull()
    val lead = best?.let { "${it.dimension.label} is your strongest area" } ?: "Your skin is well balanced"
    return lead + (focus?.let { ", and ${it.area.label.lowercase()} is where your plan will start." } ?: ". Your plan will help you keep it that way.")
}

private fun shortRegion(region: MeasurementRegion): String = when (region) {
    MeasurementRegion.FOREHEAD -> "Forehead"
    MeasurementRegion.LEFT_CHEEK -> "Left"
    MeasurementRegion.RIGHT_CHEEK -> "Right"
}

@Composable
private fun ReportContent(report: SkinReport, weekLabel: String, onArea: (String) -> Unit, onAllAreas: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val skinPrint = report.skinPrint

    FadeInUp {
        SkinthesiaCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MiniRing(value = skinPrint.overall, size = 66.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    SectionOverline(text = "$weekLabel · ${formatDay(skinPrint.createdAt)}")
                    Spacer(Modifier.height(4.dp))
                    Text(text = "SkinPrint ${skinPrint.overall}", style = typography.titleSmall, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Tag(text = skinPrint.band.label, tone = TagTone.SAGE)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(text = summaryOf(report), style = typography.body, color = colors.textSecondary)
        }
    }

    if (report.strengths.isNotEmpty()) {
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "Strengths", overline = "What's working")
        Spacer(Modifier.height(12.dp))
        FadeInUp(delayMillis = motion.stagger(1)) {
            SkinthesiaCard {
                report.strengths.forEachIndexed { index, strength ->
                    Row(Modifier.padding(vertical = 10.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier.size(26.dp).clip(CircleShape).background(colors.successSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = strength.title, style = typography.labelLarge, color = colors.textPrimary)
                            Spacer(Modifier.height(2.dp))
                            Text(text = strength.detail, style = typography.bodySmall, color = colors.textSecondary)
                            Spacer(Modifier.height(6.dp))
                            SourceBadge(strength.source)
                        }
                    }
                    if (index < report.strengths.lastIndex) SkinthesiaDivider()
                }
            }
        }
    }

    if (report.focusAreas.isNotEmpty()) {
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "Focus areas", overline = "Where to begin", actionLabel = "See all", onAction = onAllAreas)
        Spacer(Modifier.height(12.dp))
        FadeInUp(delayMillis = motion.stagger(2)) {
            SkinthesiaCard {
                val shown = report.focusAreas.take(3)
                shown.forEachIndexed { index, focus ->
                    FocusAreaRow(focus = focus, onClick = { onArea(focus.area.name) })
                    if (index < shown.lastIndex) SkinthesiaDivider()
                }
            }
        }
    }

    Spacer(Modifier.height(spacing.lg))
    SectionHeader(title = "Measured values", overline = "From your probe")
    Spacer(Modifier.height(12.dp))
    SkinthesiaCard {
        if (report.measuredValues.isEmpty()) {
            Text(
                text = "No probe readings in this analysis. Hydration was estimated from what you told us.",
                style = typography.bodySmall,
                color = colors.textSecondary,
            )
        } else {
            report.measuredValues.forEachIndexed { index, summary ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
                    Icon(summary.sensor.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(text = summary.sensor.caption, style = typography.labelLarge, color = colors.textPrimary)
                        Text(
                            text = MeasurementRegion.entries.mapNotNull { r -> summary.byRegion[r]?.let { shortRegion(r) + " " + summary.sensor.format(it) } }.joinToString("  ·  "),
                            style = typography.caption,
                            color = colors.textMuted,
                        )
                    }
                    Text(text = summary.sensor.format(summary.average), style = typography.metric, color = colors.textPrimary)
                }
                if (index < report.measuredValues.lastIndex) SkinthesiaDivider()
            }
            if (report.measuredValuesSimulated) {
                Spacer(Modifier.height(10.dp))
                SimulatedNote(text = "These readings come from the development simulator, not from real hardware.")
            }
        }
    }

    if (report.visualAnalysis.isNotEmpty()) {
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "Visual analysis", overline = "From your photo")
        Spacer(Modifier.height(12.dp))
        SkinthesiaCard {
            if (report.visualAnalysisSimulated) {
                Tag(text = "Development model", tone = TagTone.GOLD, dot = true)
                Spacer(Modifier.height(8.dp))
            }
            report.visualAnalysis.forEachIndexed { index, estimate ->
                Column(Modifier.padding(vertical = 9.dp).semantics(mergeDescendants = true) {}) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(estimate.indicator.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(text = estimate.indicator.label, style = typography.label, color = colors.textPrimary, modifier = Modifier.weight(1f))
                        Tag(text = estimate.visibility.label, tone = estimate.visibility.tagTone)
                    }
                    Spacer(Modifier.height(8.dp))
                    LevelBar(fraction = estimate.level / 100f, color = visibilityColor(estimate.visibility), delayMillis = index * 60, height = 4.dp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(text = "Bars show how visible each feature looks in your photo.", style = typography.caption, color = colors.textMuted)
        }
    }

    if (report.insights.isNotEmpty()) {
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "AI insights", overline = "Connecting the dots")
        Spacer(Modifier.height(12.dp))
        report.insights.forEachIndexed { index, insight ->
            InsightCard(insight)
            if (index < report.insights.lastIndex) Spacer(Modifier.height(10.dp))
        }
    }

    if (report.contextNotes.isNotEmpty()) {
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "Daily context", overline = "Context, not causes")
        Spacer(Modifier.height(12.dp))
        SkinthesiaCard {
            report.contextNotes.forEach { note ->
                Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                    Icon(SkinthesiaIcons.Leaf, contentDescription = null, tint = colors.success, modifier = Modifier.padding(top = 2.dp).size(14.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(text = note, style = typography.bodySmall, color = colors.textSecondary)
                }
            }
        }
    }

    Spacer(Modifier.height(spacing.lg))
    InfoNotice(
        text = "This report describes how your skin appears and reads, for tracking. It isn't a medical assessment. " +
            "If something about your skin worries you, speak to a dermatologist.",
    )
}
