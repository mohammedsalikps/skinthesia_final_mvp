package com.skinthesia.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.ai.progress.JourneyClock
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.AreaDetailRoute
import com.skinthesia.core.navigation.AreasRoute
import com.skinthesia.core.navigation.ArticleRoute
import com.skinthesia.core.navigation.CheckInIntroRoute
import com.skinthesia.core.navigation.ExpertsRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MarketplaceRoute
import com.skinthesia.core.navigation.PlanRoute
import com.skinthesia.core.navigation.RoutineRoute
import com.skinthesia.core.navigation.SkinPrintRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.DeltaBadge
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScoreRing
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.components.rememberReveal
import com.skinthesia.core.ui.formatWeekday
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.LearningArticle
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.model.stepsDueOn
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.LearningRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.analysis.LevelBar
import com.skinthesia.feature.plan.FocusAreaRow
import com.skinthesia.feature.plan.InsightCard
import com.skinthesia.feature.plan.icon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class HomeUiState(
    val loading: Boolean = true,
    val firstName: String = "",
    val latest: Assessment? = null,
    val baseline: Assessment? = null,
    val plan: PersonalizedPlan? = null,
    /** Steps done to steps due, for today. */
    val morning: Pair<Int, Int> = 0 to 0,
    val evening: Pair<Int, Int> = 0 to 0,
    val nextCheckInWeek: Int = 4,
    val currentWeek: Int = 0,
    val programmeWeeks: Int = 12,
    val programmeComplete: Boolean = false,
    val demoTimeline: Boolean = true,
    val article: LearningArticle? = null,
)

class HomeViewModel(
    private val profiles: UserProfileRepository,
    assessments: AssessmentRepository,
    plans: PlanRepository,
    learning: LearningRepository,
    settings: SettingsRepository,
    private val journeyClock: JourneyClock,
) : ViewModel() {

    val today: LocalDate = LocalDate.now()
    private val profile = MutableStateFlow(UserProfile())

    init {
        refresh()
    }

    /** Profile edits happen elsewhere; Home re-reads it whenever it becomes visible. */
    fun refresh() {
        viewModelScope.launch { profile.value = profiles.current() }
    }

    val state: StateFlow<HomeUiState> = combine(
        assessments.completed(),
        plans.currentPlan,
        plans.logsOn(today),
        learning.articles,
        combine(profile, settings.settings) { p, s -> p to s },
    ) { completed, plan, logs, articles, (p, s) ->
        val baseline = completed.firstOrNull { it.kind == AssessmentKind.BASELINE }
        val latest = completed.lastOrNull { it.skinPrint != null }
        val done = logs.map { it.stepId }.toSet()
        fun progress(time: RoutineTime): Pair<Int, Int> {
            val due = plan?.stepsDueOn(today, time).orEmpty()
            return due.count { it.id in done } to due.size
        }
        val weeks = completed.map { it.week }
        val goals = p.goals.goals.toSet()
        HomeUiState(
            loading = false,
            firstName = p.firstName,
            latest = latest,
            baseline = baseline,
            plan = plan,
            morning = progress(RoutineTime.MORNING),
            evening = progress(RoutineTime.EVENING),
            nextCheckInWeek = journeyClock.nextCheckInWeek(baseline?.startedAt, weeks, s.demoTimeline),
            currentWeek = weeks.maxOrNull() ?: 0,
            programmeWeeks = p.goals.durationWeeks,
            programmeComplete = (weeks.maxOrNull() ?: 0) >= p.goals.durationWeeks,
            demoTimeline = s.demoTimeline,
            article = articles.filter { a -> a.relevantGoals.any { it in goals } }.sortedByDescending { it.featured }.firstOrNull()
                ?: articles.firstOrNull(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

private fun SkinPrintDimension.short(): String = when (this) {
    SkinPrintDimension.CLARITY -> "Clarity"
    SkinPrintDimension.EVEN_TONE -> "Tone"
    SkinPrintDimension.TEXTURE -> "Texture"
    SkinPrintDimension.HYDRATION -> "Hydration"
    SkinPrintDimension.PORE_APPEARANCE -> "Pores"
}

/** Screen 28: the daily home. SkinPrint, today's routine, the next check-in and what to explore. */
@Composable
fun HomeScreen(onSelectTab: (Int) -> Unit) {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { HomeViewModel(profiles, assessments, plans, learning, settings, journeyClock) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    LaunchedEffect(Unit) { viewModel.refresh() }

    SkinthesiaScreen(insetBottom = false) {
        if (state.loading) {
            LoadingState(message = "Preparing your day")
            return@SkinthesiaScreen
        }
        Spacer(Modifier.height(spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = greeting() + ",", style = typography.subtitle, color = colors.textSecondary)
                Text(text = state.firstName.ifBlank { "there" }, style = typography.displayLarge, color = colors.textPrimary)
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.primaryMist)
                    .clickable(onClickLabel = "Open profile", role = Role.Button) { onSelectTab(4) },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = state.firstName.take(1).uppercase().ifBlank { "S" }, style = typography.titleSmall, color = colors.primary)
            }
        }
        Spacer(Modifier.height(6.dp))
        SectionOverline(
            text = (if (state.currentWeek == 0) "Week 1" else "Week ${state.currentWeek}") + " of ${state.programmeWeeks} · " + viewModel.today.formatWeekday(),
        )
        Spacer(Modifier.height(spacing.lg))

        val latest = state.latest
        val skinPrint = latest?.skinPrint
        if (latest != null && skinPrint != null) {
            FadeInUp {
                SkinPrintHero(
                    skinPrint = skinPrint,
                    weekLabel = latest.weekLabel,
                    baseline = state.baseline?.takeIf { it.id != latest.id }?.skinPrint,
                    onOpen = { navigator.navigate(SkinPrintRoute(latest.id)) },
                )
            }
        } else {
            EmptyState(
                icon = SkinthesiaIcons.SkinPrint,
                title = "Your SkinPrint appears here",
                body = "Complete your first analysis to see it.",
            )
        }

        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "Today's routine", overline = "Tick off as you go", actionLabel = "Plan", onAction = { navigator.navigate(PlanRoute()) })
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoutineTile(RoutineTime.MORNING, state.morning, Modifier.weight(1f).fillMaxHeight()) { navigator.navigate(RoutineRoute(RoutineTime.MORNING.name)) }
            RoutineTile(RoutineTime.EVENING, state.evening, Modifier.weight(1f).fillMaxHeight()) { navigator.navigate(RoutineRoute(RoutineTime.EVENING.name)) }
        }

        Spacer(Modifier.height(spacing.md))
        FadeInUp(delayMillis = motion.stagger(1)) {
            CheckInCard(
                week = state.nextCheckInWeek,
                demo = state.demoTimeline,
                complete = state.programmeComplete,
                onStart = { navigator.navigate(CheckInIntroRoute) },
                onJourney = { onSelectTab(1) },
            )
        }

        val focus = latest?.combined?.focusAreas.orEmpty()
        if (latest != null && focus.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(title = "Focus areas", overline = "From your latest analysis", actionLabel = "See all", onAction = { navigator.navigate(AreasRoute(latest.id)) })
            Spacer(Modifier.height(12.dp))
            SkinthesiaCard {
                val shown = focus.take(2)
                shown.forEachIndexed { index, area ->
                    FocusAreaRow(focus = area, onClick = { navigator.navigate(AreaDetailRoute(latest.id, area.area.name)) })
                    if (index < shown.lastIndex) SkinthesiaDivider()
                }
            }
        }

        latest?.combined?.insights?.firstOrNull()?.let { insight ->
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(title = "Insight", overline = "Skinthesia AI")
            Spacer(Modifier.height(12.dp))
            InsightCard(insight)
        }

        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = "Explore", overline = "Beyond your routine")
        Spacer(Modifier.height(12.dp))
        val tiles = listOf(
            ExploreItem("Shop your routine", "Products matched to you", SkinthesiaIcons.Bag) { navigator.navigate(MarketplaceRoute()) },
            ExploreItem("Talk to an expert", "Book a consultation", SkinthesiaIcons.Chat) { navigator.navigate(ExpertsRoute) },
            ExploreItem("Learn", "Evidence-based guides", SkinthesiaIcons.Learn) { onSelectTab(3) },
            ExploreItem("Community", "Questions and stories", SkinthesiaIcons.Community) { onSelectTab(3) },
        )
        tiles.chunked(2).forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { item -> ExploreTile(item, Modifier.weight(1f).fillMaxHeight()) }
            }
        }

        state.article?.let { article ->
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(title = "For you", overline = "Picked for your goals")
            Spacer(Modifier.height(12.dp))
            SkinthesiaCard(onClick = { navigator.navigate(ArticleRoute(article.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Tag(text = article.category.label, tone = TagTone.MIST)
                    Spacer(Modifier.weight(1f))
                    Text(text = "${article.readMinutes} min read", style = typography.caption, color = colors.textMuted)
                }
                Spacer(Modifier.height(10.dp))
                Text(text = article.title, style = typography.titleSmall, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(text = article.subtitle, style = typography.bodySmall, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun SkinPrintHero(skinPrint: SkinPrint, weekLabel: String, baseline: SkinPrint?, onOpen: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScoreRing(score = skinPrint.overall, size = 138.dp, strokeWidth = 7.dp, caption = null)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                SectionOverline(text = "Your SkinPrint")
                Spacer(Modifier.height(6.dp))
                Text(text = skinPrint.band.label, style = typography.titleMedium, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                if (baseline != null) {
                    val delta = skinPrint.overall - baseline.overall
                    DeltaBadge(
                        delta = delta.toDouble(),
                        improved = when {
                            delta > 0 -> true
                            delta < 0 -> false
                            else -> null
                        },
                        suffix = " since Day 1",
                    )
                } else {
                    Text(text = "Your starting point", style = typography.caption, color = colors.textMuted)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = "Updated $weekLabel", style = typography.caption, color = colors.textMuted)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth()) {
            skinPrint.scores.forEachIndexed { index, score ->
                Column(
                    modifier = Modifier.weight(1f).semantics(mergeDescendants = true) { contentDescription = "${score.dimension.label} ${score.value}" },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    VerticalBar(fraction = score.value / 100f, delayMillis = 350 + index * 90)
                    Spacer(Modifier.height(6.dp))
                    Text(text = score.value.toString(), style = typography.numeric, color = colors.textPrimary)
                    Text(text = score.dimension.short(), style = typography.caption, color = colors.textMuted, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun VerticalBar(fraction: Float, delayMillis: Int) {
    val colors = SkinthesiaTheme.colors
    val reveal = rememberReveal(fraction.coerceIn(0f, 1f), durationMillis = 900, delayMillis = delayMillis)
    Box(
        modifier = Modifier.width(8.dp).height(46.dp).clip(CircleShape).background(colors.track),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(Modifier.fillMaxWidth().fillMaxHeight(reveal.value.coerceIn(0f, 1f)).clip(CircleShape).background(colors.primary))
    }
}

@Composable
private fun RoutineTile(time: RoutineTime, progress: Pair<Int, Int>, modifier: Modifier, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val (done, due) = progress
    val complete = due > 0 && done == due
    SkinthesiaCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(time.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = time.label, style = typography.label, color = colors.textPrimary)
        }
        Spacer(Modifier.height(12.dp))
        Text(text = if (due == 0) "Rest day" else "$done of $due", style = typography.titleSmall, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        LevelBar(
            fraction = if (due == 0) 1f else done.toFloat() / due,
            color = if (complete) colors.success else colors.primary,
            height = 4.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(text = if (complete) "Complete" else "steps done", style = typography.caption, color = if (complete) colors.success else colors.textMuted)
    }
}

@Composable
private fun CheckInCard(week: Int, demo: Boolean, complete: Boolean, onStart: () -> Unit, onJourney: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val onPrimary = colors.textOnPrimary
    SkinthesiaCard(containerColor = colors.primary, borderColor = Color.Transparent) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                if (demo && !complete) {
                    Text(text = "DEMO TIMELINE", style = typography.overline, color = onPrimary.copy(alpha = 0.7f))
                    Spacer(Modifier.height(6.dp))
                }
                Text(text = if (complete) "Programme complete" else "Week $week check-in", style = typography.titleMedium, color = onPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (complete) {
                        "You've finished your programme. See how far you've come."
                    } else {
                        "A selfie and a probe reading show what's changing, and your plan adapts."
                    },
                    style = typography.bodySmall,
                    color = onPrimary.copy(alpha = 0.88f),
                )
            }
            Spacer(Modifier.width(12.dp))
            IconBadge(
                icon = if (complete) SkinthesiaIcons.Award else SkinthesiaIcons.Scan,
                container = onPrimary.copy(alpha = 0.16f),
                tint = onPrimary,
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.surface)
                .clickable(role = Role.Button, onClick = if (complete) onJourney else onStart)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text(text = if (complete) "See My Journey" else "Start Check-in", style = typography.buttonSmall, color = colors.primary)
        }
    }
}

private data class ExploreItem(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
private fun ExploreTile(item: ExploreItem, modifier: Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = modifier, onClick = item.onClick) {
        IconBadge(item.icon)
        Spacer(Modifier.height(12.dp))
        Text(text = item.title, style = typography.label, color = colors.textPrimary)
        Spacer(Modifier.height(2.dp))
        Text(text = item.subtitle, style = typography.caption, color = colors.textMuted)
    }
}
