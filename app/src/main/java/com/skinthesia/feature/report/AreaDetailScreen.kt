package com.skinthesia.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.AreaDetailRoute
import com.skinthesia.core.navigation.ExpertsRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.art.ZoneHighlight
import com.skinthesia.core.ui.art.zone
import com.skinthesia.core.ui.components.AttentionPill
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPillButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.SourceBadge
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.data.seed.AreaGuideSeed
import com.skinthesia.domain.model.FocusArea
import com.skinthesia.domain.model.ImprovementArea
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.plan.attentionColor
import com.skinthesia.feature.plan.icon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AreaDetailViewModel(
    val assessmentId: String,
    val area: ImprovementArea,
    private val assessments: AssessmentRepository,
) : ViewModel() {
    private val _focus = MutableStateFlow<FocusArea?>(null)
    val focus: StateFlow<FocusArea?> = _focus.asStateFlow()

    init {
        viewModelScope.launch {
            _focus.value = assessments.get(assessmentId)?.combined?.focusAreas?.firstOrNull { it.area == area }
        }
    }
}

/** Screen 23 detail: what we see for one area, and general guidance that may help. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AreaDetailScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<AreaDetailRoute>()
        val area = runCatching { ImprovementArea.valueOf(route.area) }.getOrDefault(ImprovementArea.HYDRATION)
        AreaDetailViewModel(route.assessmentId, area, assessments)
    }
    val focus by viewModel.focus.collectAsStateWithLifecycle()
    val guide = AreaGuideSeed.guide(viewModel.area)
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = viewModel.area.label, onBack = navigator::back) }) {
        FadeInUp {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBadge(viewModel.area.icon, size = 56.dp)
                Spacer(Modifier.height(12.dp))
                Text(text = viewModel.area.label, style = typography.title, color = colors.textPrimary)
                focus?.let { f ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AttentionPill(f.level)
                        Spacer(Modifier.width(8.dp))
                        Tag(text = "${f.score} / 100", tone = TagTone.NEUTRAL)
                        if (f.isUserGoal) {
                            Spacer(Modifier.width(8.dp))
                            Tag(text = "Your goal", tone = TagTone.CLAY)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(text = guide.headline, style = typography.articleLead, color = colors.primary, textAlign = TextAlign.Center)
            }
        }
        focus?.let { f ->
            if (f.regions.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                FaceDiagram(
                    contentDescription = "Face map highlighting " + f.regions.joinToString(", ") { it.label.lowercase() },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    highlights = f.regions.map { ZoneHighlight(it.zone(), attentionColor(f.level), intensity = 0.9f) },
                )
            }
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = motion.stagger(1)) {
                SkinthesiaCard {
                    SectionOverline(text = "What we see")
                    Spacer(Modifier.height(8.dp))
                    Text(text = f.summary, style = typography.body, color = colors.textPrimary)
                    if (f.evidence.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        f.evidence.forEach { evidence ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).semantics(mergeDescendants = true) {}) {
                                Text(text = evidence.label, style = typography.caption, color = colors.textMuted)
                                Spacer(Modifier.height(2.dp))
                                Text(text = evidence.value, style = typography.body, color = colors.textPrimary)
                                Spacer(Modifier.height(6.dp))
                                SourceBadge(evidence.source)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        GuideSection(title = "What it means", body = guide.whatItMeans)
        GuideSection(title = "How Skinthesia looks at it", body = guide.howWeLook)
        GuideList(title = "What can help", items = guide.whatCanHelp)
        if (guide.keyIngredients.isNotEmpty()) {
            Spacer(Modifier.height(spacing.md))
            SectionOverline(text = "Ingredients to look for")
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                guide.keyIngredients.forEach { Tag(text = it, tone = TagTone.MIST) }
            }
        }
        GuideList(title = "Daily habits", items = guide.dailyHabits)
        Spacer(Modifier.height(spacing.lg))
        SkinthesiaCard(containerColor = colors.blushMist) {
            Row(verticalAlignment = Alignment.Top) {
                IconBadge(SkinthesiaIcons.Chat, container = colors.surface)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = "When to see an expert", style = typography.labelLarge, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(text = guide.whenToSeeAnExpert, style = typography.bodySmall, color = colors.textSecondary)
                    Spacer(Modifier.height(10.dp))
                    SkinthesiaPillButton(text = "Find an expert", onClick = { navigator.navigate(ExpertsRoute) })
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        Text(
            text = "General skincare information, not medical advice.",
            style = typography.caption,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GuideSection(title: String, body: String) {
    val colors = SkinthesiaTheme.colors
    Spacer(Modifier.height(SkinthesiaTheme.spacing.md))
    SectionOverline(text = title)
    Spacer(Modifier.height(8.dp))
    Text(text = body, style = SkinthesiaTheme.typography.body, color = colors.textSecondary)
}

@Composable
private fun GuideList(title: String, items: List<String>) {
    if (items.isEmpty()) return
    val colors = SkinthesiaTheme.colors
    Spacer(Modifier.height(SkinthesiaTheme.spacing.md))
    SectionOverline(text = title)
    Spacer(Modifier.height(6.dp))
    items.forEach { item ->
        Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.padding(top = 8.dp).size(5.dp).clip(CircleShape).background(colors.primary))
            Spacer(Modifier.width(12.dp))
            Text(text = item, style = SkinthesiaTheme.typography.body, color = colors.textSecondary)
        }
    }
}
