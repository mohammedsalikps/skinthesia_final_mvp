package com.skinthesia.feature.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.ai.progress.JourneyClock
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CheckInIntroRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MeasurementHistoryRoute
import com.skinthesia.core.navigation.PlanRoute
import com.skinthesia.core.navigation.ProgressComparisonRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.ChartPoint
import com.skinthesia.core.ui.components.DeltaBadge
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.ListRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.components.TrendChart
import com.skinthesia.core.ui.formatDay
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.JourneyMilestone
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.ProgressSnapshot
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.ProgressUseCase
import com.skinthesia.feature.analysis.LevelBar
import com.skinthesia.feature.analysis.icon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class JourneyUiState(
    val loading: Boolean = true,
    val snapshots: List<ProgressSnapshot> = emptyList(),
    val milestones: List<JourneyMilestone> = emptyList(),
    val plans: List<PersonalizedPlan> = emptyList(),
    val programmeWeeks: Int = 12,
    val nextWeek: Int = 4,
    val demoTimeline: Boolean = true,
    val sessionCount: Int = 0,
    val hasCheckIns: Boolean = false,
)

class JourneyViewModel(
    private val progress: ProgressUseCase,
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
    private val plans: PlanRepository,
    private val settings: SettingsRepository,
    private val sessions: SkinProbeRepository,
    private val journeyClock: JourneyClock,
) : ViewModel() {

    private val _state = MutableStateFlow(JourneyUiState())
    val state: StateFlow<JourneyUiState> = _state.asStateFlow()

    /** Reloaded whenever the tab becomes visible, so a finished check-in shows immediately. */
    fun refresh() {
        viewModelScope.launch {
            val profile = profiles.current()
            val weeks = profile.goals.durationWeeks
            val completed = assessments.completedList()
            val baseline = completed.firstOrNull { it.kind == AssessmentKind.BASELINE }
            val demo = settings.current().demoTimeline
            _state.value = JourneyUiState(
                loading = false,
                snapshots = progress.snapshots(),
                milestones = progress.milestones(weeks),
                plans = plans.history().first(),
                programmeWeeks = weeks,
                nextWeek = journeyClock.nextCheckInWeek(baseline?.startedAt, completed.map { it.week }, demo),
                demoTimeline = demo,
                sessionCount = sessions.sessions().first().size,
                hasCheckIns = completed.any { it.kind == AssessmentKind.CHECK_IN },
            )
        }
    }
}

private fun weekLabel(week: Int, short: Boolean = false): String = when {
    week == 0 -> "Day 1"
    short -> "W$week"
    else -> "Week $week"
}

/** Screen 32: the 12-week journey. Trend, milestones, private photos and plan versions. */
@Composable
fun JourneyScreen(onSelectTab: (Int) -> Unit) {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { JourneyViewModel(progress, profiles, assessments, plans, settings, probeRepository, journeyClock) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    LaunchedEffect(Unit) { viewModel.refresh() }

    SkinthesiaScreen(insetBottom = false) {
        if (state.loading) {
            LoadingState(message = "Loading your journey")
            return@SkinthesiaScreen
        }
        val snapshots = state.snapshots
        val first = snapshots.firstOrNull()
        val latest = snapshots.lastOrNull()
        val currentWeek = latest?.week ?: 0
        val weeks = state.programmeWeeks

        Spacer(Modifier.height(spacing.md))
        ScreenHeader(
            overline = "Journey",
            title = "Your $weeks weeks",
            subtitle = "Every check-in adds a point. The trend matters more than any single day.",
            showMark = true,
        )
        Spacer(Modifier.height(spacing.lg))
        FadeInUp {
            SkinthesiaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SectionOverline(text = "Programme")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = (if (currentWeek == 0) "Week 1" else "Week $currentWeek") + " of $weeks",
                            style = typography.titleMedium,
                            color = colors.textPrimary,
                        )
                    }
                    if (state.demoTimeline) Tag(text = "Demo timeline", tone = TagTone.GOLD, dot = true)
                }
                Spacer(Modifier.height(12.dp))
                LevelBar(fraction = (currentWeek.toFloat() / weeks).coerceAtLeast(0.03f), color = colors.primary, height = 6.dp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    (listOf(0) + JourneyClock.MILESTONE_WEEKS).forEach { week ->
                        Text(
                            text = weekLabel(week),
                            style = typography.caption,
                            color = if (week <= currentWeek) colors.textPrimary else colors.textMuted,
                        )
                    }
                }
            }
        }

        if (latest != null && first != null) {
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = SkinthesiaTheme.motion.stagger(1)) {
                SkinthesiaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SectionOverline(text = "SkinPrint over time")
                            Spacer(Modifier.height(4.dp))
                            Text(text = latest.overall.toString(), style = typography.metricLarge, color = colors.textPrimary)
                        }
                        if (snapshots.size > 1) {
                            val delta = latest.overall - first.overall
                            DeltaBadge(
                                delta = delta.toDouble(),
                                improved = when {
                                    delta > 0 -> true
                                    delta < 0 -> false
                                    else -> null
                                },
                                suffix = " since Day 1",
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TrendChart(
                        points = snapshots.map { ChartPoint(weekLabel(it.week, short = true), it.overall.toFloat()) },
                        upcomingLabels = JourneyClock.MILESTONE_WEEKS.filter { it > currentWeek }.map { weekLabel(it, short = true) },
                        height = 170.dp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(text = "A tracking indicator for your own progress, not a medical score.", style = typography.caption, color = colors.textMuted)
                }
            }
            if (snapshots.size > 1) {
                Spacer(Modifier.height(spacing.md))
                SkinthesiaCard {
                    SectionOverline(text = "By dimension since Day 1")
                    Spacer(Modifier.height(6.dp))
                    SkinPrintDimension.entries.forEach { dimension ->
                        val from = first.scores[dimension]
                        val to = latest.scores[dimension]
                        if (from != null && to != null) {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 7.dp).semantics(mergeDescendants = true) {},
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(dimension.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(text = dimension.label, style = typography.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                                Text(text = "$from → $to", style = typography.numeric, color = colors.textSecondary)
                                Spacer(Modifier.width(8.dp))
                                DeltaBadge(
                                    delta = (to - from).toDouble(),
                                    improved = when {
                                        to > from -> true
                                        to < from -> false
                                        else -> null
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.milestones.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(title = "Milestones", overline = "Your timeline")
            Spacer(Modifier.height(12.dp))
            SkinthesiaCard {
                state.milestones.forEachIndexed { index, milestone ->
                    MilestoneRow(milestone = milestone, isLast = index == state.milestones.lastIndex)
                }
            }
        }

        val photos = snapshots.filter { it.photoPath != null }
        if (photos.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(
                title = "Your photos",
                overline = "Private to this device",
                actionLabel = if (state.hasCheckIns) "Compare" else null,
                onAction = if (state.hasCheckIns) ({ navigator.navigate(ProgressComparisonRoute()) }) else null,
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                photos.forEach { snapshot ->
                    Column(Modifier.width(100.dp)) {
                        Box(Modifier.size(width = 100.dp, height = 128.dp).clip(SkinthesiaTheme.shapes.tile).background(colors.surfaceMuted)) {
                            SkinthesiaImage(
                                source = ImageSource.LocalFile(snapshot.photoPath.orEmpty()),
                                contentDescription = "Selfie from ${snapshot.weekLabel}",
                                modifier = Modifier.fillMaxSize(),
                                maxDimension = 400,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(text = snapshot.weekLabel, style = typography.label, color = colors.textPrimary)
                        Text(text = "SkinPrint ${snapshot.overall}", style = typography.caption, color = colors.textMuted)
                    }
                }
            }
        }

        if (state.plans.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(title = "Plan versions", overline = "How your plan evolved", actionLabel = "Open plan", onAction = { navigator.navigate(PlanRoute()) })
            Spacer(Modifier.height(12.dp))
            SkinthesiaCard {
                state.plans.forEachIndexed { index, plan ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp).semantics(mergeDescendants = true) {}) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Version ${plan.version}", style = typography.labelLarge, color = colors.textPrimary)
                            if (index == 0) {
                                Spacer(Modifier.width(8.dp))
                                Tag(text = "Current", tone = TagTone.SAGE)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(text = plan.focusTitle + " · " + formatDay(plan.createdAt), style = typography.caption, color = colors.textMuted)
                        if (plan.changes.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = plan.changes.joinToString(" · ") { it.title },
                                style = typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                    if (index < state.plans.lastIndex) SkinthesiaDivider()
                }
            }
        }

        Spacer(Modifier.height(spacing.lg))
        SkinthesiaCard {
            ListRow(
                title = "Week ${state.nextWeek} check-in",
                subtitle = "Selfie, probe reading and comparison",
                leadingIcon = SkinthesiaIcons.Scan,
                onClick = { navigator.navigate(CheckInIntroRoute) },
            )
            SkinthesiaDivider()
            ListRow(
                title = "Measurement history",
                subtitle = if (state.sessionCount == 1) "1 probe session" else "${state.sessionCount} probe sessions",
                leadingIcon = SkinthesiaIcons.Clock,
                onClick = { navigator.navigate(MeasurementHistoryRoute) },
            )
        }
        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun MilestoneRow(milestone: JourneyMilestone, isLast: Boolean) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val achieved = !milestone.isUpcoming
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).semantics(mergeDescendants = true) {}) {
        Column(Modifier.width(28.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (achieved) colors.success else colors.surface)
                    .border(1.5.dp, if (achieved) colors.success else colors.borderStrong, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (achieved) Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.textOnPrimary, modifier = Modifier.size(11.dp))
            }
            if (!isLast) {
                Box(
                    Modifier
                        .padding(vertical = 4.dp)
                        .width(1.5.dp)
                        .weight(1f)
                        .background(if (achieved) colors.success.copy(alpha = 0.4f) else colors.border),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 18.dp)) {
            SectionOverline(text = weekLabel(milestone.week))
            Spacer(Modifier.height(2.dp))
            Text(text = milestone.title, style = typography.labelLarge, color = if (achieved) colors.textPrimary else colors.textSecondary)
            Text(text = milestone.detail, style = typography.bodySmall, color = colors.textSecondary)
            milestone.achievedAt?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = formatDay(it), style = typography.caption, color = colors.textMuted)
            }
        }
    }
}
