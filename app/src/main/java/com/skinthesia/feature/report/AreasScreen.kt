package com.skinthesia.feature.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.AreaDetailRoute
import com.skinthesia.core.navigation.AreasRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.art.ZoneHighlight
import com.skinthesia.core.ui.art.zone
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LegendDot
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.domain.model.FocusArea
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.feature.plan.FocusAreaRow
import com.skinthesia.feature.plan.attentionColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AreasUiState(val loading: Boolean = true, val areas: List<FocusArea> = emptyList())

class AreasViewModel(val assessmentId: String, private val assessments: AssessmentRepository) : ViewModel() {
    private val _state = MutableStateFlow(AreasUiState())
    val state: StateFlow<AreasUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = AreasUiState(loading = false, areas = assessments.get(assessmentId)?.combined?.focusAreas.orEmpty())
        }
    }
}

/** Screen 23: every focus area on a face map, each opening a plain-language guide. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AreasScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> AreasViewModel(handle.toRoute<AreasRoute>().assessmentId, assessments) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = "Areas for improvement", onBack = navigator::back) }) {
        if (state.loading) {
            LoadingState(message = "Loading your focus areas")
            return@SkinthesiaScreen
        }
        ScreenHeader(
            title = "Where to focus",
            subtitle = "Ranked by how much attention each area may need. Tap any area to learn more.",
        )
        Spacer(Modifier.height(spacing.md))
        val byZone = state.areas.flatMap { area -> area.regions.map { it.zone() to area.level } }
            .groupBy({ it.first }, { it.second })
        val highlights = byZone.map { (zone, levels) -> ZoneHighlight(zone, attentionColor(levels.maxBy { it.ordinal }), intensity = 0.9f) }
        val levels = state.areas.map { it.level }.distinct().sortedByDescending { it.ordinal }
        FadeInUp {
            SkinthesiaCard {
                FaceDiagram(
                    contentDescription = "Face map highlighting " + state.areas.joinToString(", ") { it.area.label.lowercase() },
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    highlights = highlights,
                    showSideLabels = true,
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    levels.forEach { level -> LegendDot(label = level.label, color = attentionColor(level)) }
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        FadeInUp(delayMillis = SkinthesiaTheme.motion.stagger(1)) {
            SkinthesiaCard {
                state.areas.forEachIndexed { index, focus ->
                    FocusAreaRow(
                        focus = focus,
                        showBar = true,
                        delayMillis = 200 + index * 90,
                        onClick = { navigator.navigate(AreaDetailRoute(viewModel.assessmentId, focus.area.name)) },
                    )
                    if (index < state.areas.lastIndex) SkinthesiaDivider()
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        InfoNotice(text = "Focus areas describe appearance and readings to guide your routine. They aren't diagnoses.")
    }
}
