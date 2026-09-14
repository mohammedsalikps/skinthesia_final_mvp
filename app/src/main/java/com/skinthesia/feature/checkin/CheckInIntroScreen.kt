package com.skinthesia.feature.checkin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.ai.progress.JourneyClock
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AdherenceSummary
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.ProgressUseCase
import com.skinthesia.domain.usecase.StartAssessmentUseCase
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.analysis.LevelBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckInIntroUiState(
    val loading: Boolean = true,
    val week: Int = 4,
    val lastLabel: String? = null,
    val demoTimeline: Boolean = true,
    val programmeComplete: Boolean = false,
    val adherence: AdherenceSummary? = null,
    val starting: Boolean = false,
)

class CheckInIntroViewModel(
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
    private val plans: PlanRepository,
    private val settings: SettingsRepository,
    private val journeyClock: JourneyClock,
    private val progress: ProgressUseCase,
    private val startAssessment: StartAssessmentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckInIntroUiState())
    val state: StateFlow<CheckInIntroUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = assessments.completedList()
            val baseline = completed.firstOrNull { it.kind == AssessmentKind.BASELINE }
            val demo = settings.current().demoTimeline
            val weeks = completed.map { it.week }
            val profile = profiles.current()
            _state.value = CheckInIntroUiState(
                loading = false,
                week = journeyClock.nextCheckInWeek(baseline?.startedAt, weeks, demo),
                lastLabel = completed.lastOrNull()?.takeIf { it.kind == AssessmentKind.CHECK_IN }?.weekLabel,
                demoTimeline = demo,
                programmeComplete = (weeks.maxOrNull() ?: 0) >= profile.goals.durationWeeks,
                adherence = plans.current()?.let { progress.adherence(it) },
            )
        }
    }

    /** Starts (or resumes) the check-in assessment and hands its id to the capture flow. */
    fun start(onStarted: (String) -> Unit) {
        if (_state.value.starting) return
        _state.update { it.copy(starting = true) }
        viewModelScope.launch {
            val assessment = startAssessment(AssessmentKind.CHECK_IN)
            _state.update { it.copy(starting = false) }
            onStarted(assessment.id)
        }
    }
}

/** Screen 29: what the weekly check-in involves, and how the routine has gone so far. */
@Composable
fun CheckInIntroScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel {
        CheckInIntroViewModel(profiles, assessments, plans, settings, journeyClock, progress, startAssessment)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Weekly check-in", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = "Start Check-in",
                loading = state.starting,
                enabled = !state.loading && !state.starting,
                onClick = { viewModel.start { id -> navigator.startCapture(id, FlowKind.CHECK_IN) } },
            )
        },
    ) {
        if (state.loading) {
            LoadingState(message = "Preparing your check-in")
            return@SkinthesiaScreen
        }
        FadeInUp {
            Column {
                SectionOverline(text = if (state.programmeComplete) "Bonus check-in" else "Check-in")
                Spacer(Modifier.height(8.dp))
                Text(text = "Week ${state.week}", style = typography.hero, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Three short steps. We'll compare what we see with Day 1" + (state.lastLabel?.let { " and your last check-in ($it)." } ?: "."),
                    style = typography.body,
                    color = colors.textSecondary,
                )
                if (state.demoTimeline) {
                    Spacer(Modifier.height(12.dp))
                    Tag(text = "Demo timeline", tone = TagTone.GOLD, dot = true)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Each check-in advances to the next milestone week (4, 8, 12), so the full programme can be shown in one sitting. Turn this off in Profile › Data controls.",
                        style = typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
        }
        Spacer(Modifier.height(spacing.lg))
        FadeInUp(delayMillis = motion.stagger(1)) {
            SkinthesiaCard {
                val steps = listOf(
                    Triple(SkinthesiaIcons.Camera, "Selfie", "Same light and angle as last time, no makeup."),
                    Triple(SkinthesiaIcons.Probe, "Probe reading", "Forehead and both cheeks. You can skip it if needed."),
                    Triple(SkinthesiaIcons.Trend, "Compare and adapt", "See what changed, then your plan adjusts."),
                )
                steps.forEachIndexed { index, (icon, title, detail) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(icon)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = "${index + 1}. $title", style = typography.labelLarge, color = colors.textPrimary)
                            Text(text = detail, style = typography.bodySmall, color = colors.textSecondary)
                        }
                    }
                    if (index < steps.lastIndex) SkinthesiaDivider()
                }
            }
        }
        state.adherence?.let { adherence ->
            Spacer(Modifier.height(spacing.md))
            SkinthesiaCard {
                SectionOverline(text = "Your routine lately")
                Spacer(Modifier.height(8.dp))
                if (adherence.scheduledSteps == 0) {
                    Text(text = "Your routine log starts today. Tick steps off as you go.", style = typography.body, color = colors.textSecondary)
                } else {
                    Text(
                        text = "${adherence.completedSteps} of ${adherence.scheduledSteps} steps completed",
                        style = typography.titleSmall,
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(10.dp))
                    LevelBar(fraction = adherence.rate, color = colors.success)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (adherence.streakDays > 0) "${adherence.streakDays}-day streak. Consistency matters more than perfection." else "Every step counts. Consistency matters more than perfection.",
                        style = typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        InfoNotice(text = "For a fair comparison, use the same room and time of day as your first selfie.")
    }
}
