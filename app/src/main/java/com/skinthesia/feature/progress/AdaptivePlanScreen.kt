package com.skinthesia.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.AdaptivePlanRoute
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.PlanRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MiniRing
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaSecondaryButton
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AdherenceSummary
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.PlanChangeKind
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.usecase.ProgressUseCase
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.onboarding.icon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

data class AdaptiveUiState(
    val loading: Boolean = true,
    val current: PersonalizedPlan? = null,
    val previous: PersonalizedPlan? = null,
    val adherence: AdherenceSummary? = null,
)

class AdaptivePlanViewModel(
    val flow: FlowKind,
    plans: PlanRepository,
    private val progress: ProgressUseCase,
) : ViewModel() {

    private val adherence = MutableStateFlow<AdherenceSummary?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val history = plans.history().mapLatest { list ->
        list.firstOrNull()?.let { adherence.value = progress.adherence(it) }
        list
    }

    val state: StateFlow<AdaptiveUiState> = combine(history, adherence) { list, summary ->
        AdaptiveUiState(loading = false, current = list.firstOrNull(), previous = list.getOrNull(1), adherence = summary)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdaptiveUiState())
}

private val PlanChangeKind.icon: ImageVector
    get() = when (this) {
        PlanChangeKind.FOCUS_SHIFT -> SkinthesiaIcons.Target
        PlanChangeKind.STEP_ADDED -> SkinthesiaIcons.Plus
        PlanChangeKind.STEP_REMOVED -> SkinthesiaIcons.Minus
        PlanChangeKind.STEP_ADJUSTED -> SkinthesiaIcons.Refresh
        PlanChangeKind.PRODUCT_SWAPPED -> SkinthesiaIcons.Layers
        PlanChangeKind.KEEP -> SkinthesiaIcons.Check
    }

/** Screen 31: how and why the plan changed after a check-in. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdaptivePlanScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        AdaptivePlanViewModel(handle.toRoute<AdaptivePlanRoute>().flow.toFlowKind(), plans, progress)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val checkIn = viewModel.flow == FlowKind.CHECK_IN

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Adaptive plan", onBack = if (checkIn) null else navigator::back) },
        bottomBar = {
            if (checkIn) {
                SkinthesiaPrimaryButton(text = "Back to Home", onClick = navigator::goHome)
                SkinthesiaSecondaryButton(text = "Open My Plan", onClick = { navigator.navigate(PlanRoute()) })
            } else {
                SkinthesiaPrimaryButton(text = "Open My Plan", onClick = { navigator.navigate(PlanRoute()) })
            }
        },
    ) {
        val plan = state.current
        when {
            state.loading -> LoadingState(message = "Updating your plan")
            plan == null -> EmptyState(icon = SkinthesiaIcons.Layers, title = "No plan yet", body = "Your plan appears after your first analysis.")
            else -> {
                FadeInUp {
                    Column {
                        SectionOverline(text = "Plan version ${plan.version}")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (state.previous == null) "Your plan" else "Your plan has adapted",
                            style = typography.display,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Based on your latest check-in and how your routine has gone.",
                            style = typography.body,
                            color = colors.textSecondary,
                        )
                    }
                }
                state.adherence?.let { adherence ->
                    Spacer(Modifier.height(spacing.lg))
                    SkinthesiaCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MiniRing(value = (adherence.rate * 100).toInt(), size = 58.dp, color = colors.success, label = "${(adherence.rate * 100).toInt()}%")
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                SectionOverline(text = "Routine consistency")
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (adherence.scheduledSteps == 0) "No routine steps logged yet" else "${adherence.completedSteps} of ${adherence.scheduledSteps} steps",
                                    style = typography.labelLarge,
                                    color = colors.textPrimary,
                                )
                                Text(
                                    text = if (adherence.streakDays > 0) "${adherence.streakDays}-day streak" else "Over the last two weeks",
                                    style = typography.caption,
                                    color = colors.textMuted,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                SectionHeader(title = "What changed", overline = "And why")
                Spacer(Modifier.height(12.dp))
                FadeInUp(delayMillis = motion.stagger(1)) {
                    SkinthesiaCard {
                        if (plan.changes.isEmpty()) {
                            Text(text = "No changes needed. Your plan is working, so keep going.", style = typography.body, color = colors.textSecondary)
                        }
                        plan.changes.forEachIndexed { index, change ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.Top) {
                                IconBadge(change.kind.icon)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = change.title, style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
                                        Tag(text = change.kind.label, tone = TagTone.NEUTRAL)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = change.detail, style = typography.bodySmall, color = colors.textSecondary)
                                }
                            }
                            if (index < plan.changes.lastIndex) SkinthesiaDivider()
                        }
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                SectionHeader(title = "Focus now", overline = plan.focusTitle)
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plan.focus.forEach { goal -> Tag(text = goal.label, tone = TagTone.CLAY, icon = goal.icon) }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Morning routine: ${plan.morning.steps.size} steps · Evening routine: ${plan.evening.steps.size} steps",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(spacing.lg))
                InfoNotice(
                    text = "Plans adapt with simple, transparent rules: progress on your focus areas, how consistently you followed your routine, and your preferences.",
                )
            }
        }
    }
}
