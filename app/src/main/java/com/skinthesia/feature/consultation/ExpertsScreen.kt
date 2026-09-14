package com.skinthesia.feature.consultation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.BookingRoute
import com.skinthesia.core.navigation.ExpertProfileRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MyBookingsRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.IconAction
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MonogramAvatar
import com.skinthesia.core.ui.components.RatingLine
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.components.fmt
import com.skinthesia.core.ui.formatPrice
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.BookingStatus
import com.skinthesia.domain.model.ConsultationExpert
import com.skinthesia.domain.model.ConsultationType
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.repository.ConsultationRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.analysis.IconBadge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val ConsultationType.icon: ImageVector
    get() = when (this) {
        ConsultationType.VIDEO -> SkinthesiaIcons.Video
        ConsultationType.CHAT -> SkinthesiaIcons.Chat
        ConsultationType.SKINPRINT_REVIEW -> SkinthesiaIcons.SkinPrint
    }

data class ExpertsUiState(
    val loading: Boolean = true,
    val matched: List<ConsultationExpert> = emptyList(),
    val others: List<ConsultationExpert> = emptyList(),
    val upcoming: Int = 0,
)

class ExpertsViewModel(
    consultations: ConsultationRepository,
    profiles: UserProfileRepository,
    private val clock: () -> Long,
) : ViewModel() {

    private val goals = MutableStateFlow<Set<SkinGoal>?>(null)

    init {
        viewModelScope.launch { goals.value = profiles.current().goals.goals.toSet() }
    }

    val state: StateFlow<ExpertsUiState> = combine(consultations.experts, consultations.bookings, goals.filterNotNull()) { experts, bookings, userGoals ->
        val (matched, others) = experts.sortedByDescending { it.rating }.partition { expert -> expert.focusGoals.any { it in userGoals } }
        ExpertsUiState(
            loading = false,
            matched = matched,
            others = others,
            upcoming = bookings.count { it.status == BookingStatus.CONFIRMED && it.startAt > clock() },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpertsUiState())
}

/** Consultation home: experts matched to the user's goals first. */
@Composable
fun ExpertsScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { ExpertsViewModel(consultations, profiles, clock) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    SkinthesiaScreen(
        topBar = {
            SkinthesiaTopBar(
                title = "Experts",
                onBack = navigator::back,
                actions = {
                    IconAction(
                        icon = SkinthesiaIcons.Calendar,
                        contentDescription = "My bookings",
                        onClick = { navigator.navigate(MyBookingsRoute) },
                        badgeCount = state.upcoming,
                    )
                },
            )
        },
    ) {
        if (state.loading) {
            LoadingState(message = "Loading experts")
            return@SkinthesiaScreen
        }
        ScreenHeader(
            title = "Talk to an expert",
            subtitle = "Dermatologists and skin specialists who can review your SkinPrint and answer your questions.",
        )
        Spacer(Modifier.height(spacing.lg))
        if (state.matched.isNotEmpty()) {
            SectionHeader(title = "Matched to your goals", overline = "Recommended")
            Spacer(Modifier.height(12.dp))
            state.matched.forEachIndexed { index, expert ->
                FadeInUp(delayMillis = motion.stagger(index.coerceAtMost(3))) {
                    ExpertCard(expert = expert, matched = true) { navigator.navigate(ExpertProfileRoute(expert.id)) }
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(spacing.sm))
        }
        if (state.others.isNotEmpty()) {
            SectionHeader(title = if (state.matched.isEmpty()) "All experts" else "More experts")
            Spacer(Modifier.height(12.dp))
            state.others.forEach { expert ->
                ExpertCard(expert = expert, matched = false) { navigator.navigate(ExpertProfileRoute(expert.id)) }
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(spacing.sm))
        InfoNotice(
            text = "Expert profiles are demonstration content and bookings are simulated. No consultation takes place and no payment is taken.",
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpertCard(expert: ConsultationExpert, matched: Boolean, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            MonogramAvatar(name = expert.name, size = 54.dp, isExpert = true)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(text = expert.name, style = typography.labelLarge, color = colors.textPrimary)
                Text(text = expert.title, style = typography.bodySmall, color = colors.textSecondary)
                Spacer(Modifier.height(6.dp))
                RatingLine(rating = expert.rating, reviewCount = expert.reviewCount)
            }
            Icon(SkinthesiaIcons.ChevronRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            expert.specialties.take(3).forEach { Tag(text = it, tone = TagTone.MIST) }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${expert.yearsExperience} years · " + expert.languages.joinToString(", "),
                style = typography.caption,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(text = "from " + formatPrice(expert.fee), style = typography.label, color = colors.textPrimary)
        }
        if (matched) {
            Spacer(Modifier.height(10.dp))
            Tag(text = "Matches your goals", tone = TagTone.SAGE, icon = SkinthesiaIcons.Check)
        }
    }
}

class ExpertProfileViewModel(val expertId: String, consultations: ConsultationRepository) : ViewModel() {
    private val _expert = MutableStateFlow<ConsultationExpert?>(null)
    val expert: StateFlow<ConsultationExpert?> = _expert.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            _expert.value = consultations.expert(expertId)
            _loading.value = false
        }
    }
}

/** An expert's profile: credentials, approach, ways to consult and member reviews. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpertProfileScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> ExpertProfileViewModel(handle.toRoute<ExpertProfileRoute>().expertId, consultations) }
    val expert by viewModel.expert.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Expert profile", onBack = navigator::back) },
        bottomBar = expert?.let { e ->
            { SkinthesiaPrimaryButton(text = "Book Consultation", onClick = { navigator.navigate(BookingRoute(e.id)) }) }
        },
    ) {
        val e = expert
        when {
            loading -> LoadingState(message = "Loading profile")
            e == null -> ScreenHeader(title = "Expert not found", centered = true)
            else -> {
                FadeInUp {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        MonogramAvatar(name = e.name, size = 88.dp, isExpert = true)
                        Spacer(Modifier.height(14.dp))
                        Text(text = e.name, style = typography.title, color = colors.textPrimary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(text = e.title, style = typography.subtitle, color = colors.textSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(text = e.credentials, style = typography.caption, color = colors.textMuted, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        RatingLine(rating = e.rating, reviewCount = e.reviewCount)
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(value = "${e.yearsExperience}", label = "years", modifier = Modifier.weight(1f).fillMaxHeight())
                    StatTile(value = fmt(e.rating, 1), label = "rating", modifier = Modifier.weight(1f).fillMaxHeight())
                    StatTile(value = formatPrice(e.fee), label = "per session", modifier = Modifier.weight(1f).fillMaxHeight())
                }
                Spacer(Modifier.height(spacing.lg))
                SectionOverline(text = "Specialties")
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    e.specialties.forEach { Tag(text = it, tone = TagTone.MIST) }
                }
                Spacer(Modifier.height(spacing.lg))
                SectionOverline(text = "About")
                Spacer(Modifier.height(8.dp))
                Text(text = e.bio, style = typography.body, color = colors.textSecondary)
                Spacer(Modifier.height(spacing.md))
                SectionOverline(text = "Approach")
                Spacer(Modifier.height(8.dp))
                Text(text = e.approach, style = typography.articleLead, color = colors.primary)
                Spacer(Modifier.height(spacing.lg))
                SectionOverline(text = "Ways to consult")
                Spacer(Modifier.height(10.dp))
                e.consultationTypes.sortedBy { it.ordinal }.forEach { type ->
                    SkinthesiaCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(type.icon)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(text = type.label, style = typography.labelLarge, color = colors.textPrimary)
                                Text(text = type.description, style = typography.bodySmall, color = colors.textSecondary)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(text = "${type.durationMinutes} min", style = typography.caption, color = colors.textMuted)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(spacing.sm))
                SectionOverline(text = "Languages")
                Spacer(Modifier.height(6.dp))
                Text(text = e.languages.joinToString(", "), style = typography.body, color = colors.textPrimary)
                if (e.reviews.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.lg))
                    SectionOverline(text = "What members say")
                    Spacer(Modifier.height(10.dp))
                    e.reviews.forEach { review ->
                        SkinthesiaCard {
                            RatingLine(rating = review.rating.toFloat())
                            Spacer(Modifier.height(8.dp))
                            Text(text = "“${review.text}”", style = typography.body, color = colors.textPrimary)
                            Spacer(Modifier.height(6.dp))
                            Text(text = "— " + review.author, style = typography.caption, color = colors.textMuted)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                Spacer(Modifier.height(spacing.sm))
                InfoNotice(text = "Demonstration profile. Experts support skincare decisions and don't replace in-person medical care.")
            }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = modifier, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = typography.titleSmall, color = colors.textPrimary, maxLines = 1)
            Text(text = label, style = typography.caption, color = colors.textMuted)
        }
    }
}
