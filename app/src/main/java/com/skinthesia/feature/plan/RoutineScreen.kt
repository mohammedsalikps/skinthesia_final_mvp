package com.skinthesia.feature.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.RoutineRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.CheckCircle
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MiniRing
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatWeekday
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.RoutineStep
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.domain.model.StepFrequency
import com.skinthesia.domain.model.isDueOn
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RoutineUiState(
    val loading: Boolean = true,
    val hasPlan: Boolean = false,
    val steps: List<RoutineStep> = emptyList(),
    val done: Set<String> = emptySet(),
    val products: Map<String, Product> = emptyMap(),
)

class RoutineViewModel(
    val time: RoutineTime,
    private val plans: PlanRepository,
    catalog: ProductCatalogRepository,
) : ViewModel() {

    val today: LocalDate = LocalDate.now()

    val state: StateFlow<RoutineUiState> = combine(plans.currentPlan, plans.logsOn(today), catalog.products) { plan, logs, products ->
        RoutineUiState(
            loading = false,
            hasPlan = plan != null,
            steps = plan?.routine(time)?.steps.orEmpty().sortedBy { it.order },
            done = logs.map { it.stepId }.toSet(),
            products = products.associateBy { it.id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoutineUiState())

    fun toggle(stepId: String, done: Boolean) {
        viewModelScope.launch { plans.setStepDone(today, stepId, done) }
    }
}

/** Screens 25 and 26: today's morning or evening routine, ticked off step by step. */
@Composable
fun RoutineScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val time = runCatching { RoutineTime.valueOf(handle.toRoute<RoutineRoute>().time) }.getOrDefault(RoutineTime.MORNING)
        RoutineViewModel(time, plans, catalog)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val time = viewModel.time
    val other = if (time == RoutineTime.MORNING) RoutineTime.EVENING else RoutineTime.MORNING
    val due = state.steps.filter { it.isDueOn(viewModel.today) }
    val doneCount = due.count { it.id in state.done }

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "${time.label} routine", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaTextButton(
                text = "Switch to ${other.label.lowercase()} routine",
                onClick = { navigator.replace(RoutineRoute(other.name)) },
            )
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Loading your routine")
            !state.hasPlan -> EmptyState(
                icon = time.icon,
                title = "No routine yet",
                body = "Your routine appears once your first analysis is complete.",
            )
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SectionOverline(text = viewModel.today.formatWeekday())
                        Spacer(Modifier.height(6.dp))
                        Text(text = "${time.label} routine", style = typography.title, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (due.isEmpty()) "Nothing scheduled today" else "$doneCount of ${due.size} done today",
                            style = typography.body,
                            color = colors.textSecondary,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                    MiniRing(
                        value = if (due.isEmpty()) 100 else doneCount * 100 / due.size,
                        size = 60.dp,
                        label = if (due.isEmpty()) "–" else "$doneCount/${due.size}",
                        color = if (due.isNotEmpty() && doneCount == due.size) colors.success else colors.primary,
                    )
                }
                if (due.isNotEmpty() && doneCount == due.size) {
                    Spacer(Modifier.height(spacing.md))
                    FadeInUp {
                        SkinthesiaCard(containerColor = colors.successSoft, borderColor = Color.Transparent) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.successStrong, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(text = "${time.label} routine complete. Nicely done.", style = typography.label, color = colors.successStrong)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                state.steps.forEachIndexed { index, step ->
                    FadeInUp(delayMillis = motion.stagger(index.coerceAtMost(4))) {
                        StepCard(
                            index = index,
                            step = step,
                            product = step.productId?.let(state.products::get),
                            due = step.isDueOn(viewModel.today),
                            done = step.id in state.done,
                            onToggle = { viewModel.toggle(step.id, it) },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                InfoNotice(text = "Tick each step as you go. Your weekly check-ins use this to adapt your plan.")
            }
        }
    }
}

@Composable
private fun StepCard(index: Int, step: RoutineStep, product: Product?, due: Boolean, done: Boolean, onToggle: (Boolean) -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val toggle = if (due) {
        Modifier.clip(SkinthesiaTheme.shapes.card).toggleable(value = done, role = Role.Checkbox, onValueChange = onToggle)
    } else {
        Modifier
    }
    SkinthesiaCard(modifier = Modifier.fillMaxWidth().then(toggle), containerColor = if (done) colors.surfaceMuted else colors.surface) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = (index + 1).toString().padStart(2, '0'),
                style = typography.metric,
                color = if (done) colors.textMuted else colors.primary,
                modifier = Modifier.width(40.dp),
            )
            Column(Modifier.weight(1f)) {
                SectionOverline(text = step.type.label)
                Spacer(Modifier.height(4.dp))
                Text(text = step.title, style = typography.titleSmall, color = if (done) colors.textSecondary else colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(text = step.instruction, style = typography.bodySmall, color = colors.textSecondary)
                if (product != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductThumb(product, size = 44.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = product.brand, style = typography.caption, color = colors.textMuted)
                            Text(
                                text = if (product.name == step.title) product.category.label + " · " + product.size else product.name,
                                style = typography.label,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else if (step.keepCurrentProduct) {
                    Spacer(Modifier.height(8.dp))
                    Tag(text = "Use your current product", tone = TagTone.NEUTRAL)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = "Why: " + step.purpose, style = typography.caption, color = colors.textMuted)
                if (step.frequency != StepFrequency.DAILY || !due) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (step.frequency != StepFrequency.DAILY) Tag(text = step.frequency.label, tone = TagTone.MIST)
                        if (!due) Tag(text = "Not today", tone = TagTone.NEUTRAL)
                    }
                }
            }
            if (due) {
                Spacer(Modifier.width(8.dp))
                CheckCircle(selected = done, size = 26.dp)
            }
        }
    }
}
