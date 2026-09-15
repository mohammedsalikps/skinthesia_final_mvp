package com.skinthesia.feature.learn

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.ArticleRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.ArticleArtwork
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.IconAction
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.ScrollableFilterTabs
import com.skinthesia.core.ui.components.SearchField
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SegmentedTabs
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.ArticleCategory
import com.skinthesia.domain.model.LearningArticle
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.repository.LearningRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.community.CommunityFeed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LearnUiState(
    val loading: Boolean = true,
    val articles: List<LearningArticle> = emptyList(),
    val saved: Set<String> = emptySet(),
    val goals: Set<SkinGoal> = emptySet(),
)

class LearnViewModel(
    private val learning: LearningRepository,
    profiles: UserProfileRepository,
    settings: SettingsRepository,
) : ViewModel() {
    val state: StateFlow<LearnUiState> = combine(learning.articles, learning.savedIds, profiles.profile, settings.settings) { articles, saved, profile, s ->
        LearnUiState(loading = false, articles = articles, saved = saved, goals = if (s.personalizedContent) profile.goals.goals.toSet() else emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnUiState())

    fun toggleSaved(id: String) {
        viewModelScope.launch { learning.toggleSaved(id) }
    }
}

private val LEARN_SEGMENTS = listOf("Library", "Community")

/** The Learn tab: an editorial library and a supportive community. */
@Composable
fun LearnScreen() {
    val spacing = SkinthesiaTheme.spacing
    var segment by rememberSaveable { mutableIntStateOf(0) }

    SkinthesiaScreen(insetBottom = false) {
        Spacer(Modifier.height(spacing.md))
        ScreenHeader(
            overline = "Learn",
            title = if (segment == 0) "Skin, explained" else "Community",
            subtitle = if (segment == 0) {
                "Clear, evidence-informed guides written with our specialists."
            } else {
                "Questions, routines and stories, shared with care."
            },
            showMark = true,
        )
        Spacer(Modifier.height(spacing.md))
        SegmentedTabs(options = LEARN_SEGMENTS, selectedIndex = segment, onSelect = { segment = it })
        Spacer(Modifier.height(spacing.lg))
        if (segment == 0) LibraryContent() else CommunityFeed()
        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun LibraryContent() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { LearnViewModel(learning, profiles, settings) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    var query by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val categories: List<ArticleCategory?> = listOf<ArticleCategory?>(null) + ArticleCategory.entries

    if (state.loading) {
        LoadingState(message = "Loading guides")
        return
    }
    val open: (LearningArticle) -> Unit = { navigator.navigate(ArticleRoute(it.id)) }
    val filter = categories.getOrNull(selected)
    val q = query.trim()
    val shown = state.articles.filter { article ->
        (filter == null || article.category == filter) &&
            (q.isEmpty() || article.title.contains(q, ignoreCase = true) || article.subtitle.contains(q, ignoreCase = true))
    }

    SearchField(value = query, onValueChange = { query = it }, placeholder = "Search guides")
    Spacer(Modifier.height(spacing.md))

    if (q.isEmpty() && filter == null) {
        val featured = state.articles.firstOrNull { it.featured } ?: state.articles.firstOrNull()
        featured?.let { article ->
            FadeInUp { FeaturedArticleCard(article = article, onOpen = { open(article) }) }
            Spacer(Modifier.height(spacing.lg))
        }
        val forYou = state.articles.filter { a -> a.id != featured?.id && a.relevantGoals.any { it in state.goals } }.take(5)
        if (forYou.isNotEmpty()) {
            SectionHeader(title = "For your goals", overline = "Personalised")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                forYou.forEach { article -> CompactArticleCard(article = article, onOpen = { open(article) }) }
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }

    SectionHeader(title = filter?.label ?: "All guides", overline = "${shown.size} guides")
    Spacer(Modifier.height(12.dp))
    ScrollableFilterTabs(
        options = categories.map { it?.label ?: "All" },
        selectedIndex = selected,
        onSelect = { selected = it },
        contentPadding = PaddingValues(0.dp),
    )
    Spacer(Modifier.height(spacing.md))
    if (shown.isEmpty()) {
        EmptyState(icon = SkinthesiaIcons.Search, title = "No guides match", body = "Try another word or category.")
    }
    shown.forEach { article ->
        ArticleRow(article = article, saved = article.id in state.saved, onOpen = { open(article) }, onToggleSave = { viewModel.toggleSaved(article.id) })
        Spacer(Modifier.height(12.dp))
    }
    val saved = state.articles.filter { it.id in state.saved }
    if (saved.isNotEmpty() && q.isEmpty() && filter == null) {
        Spacer(Modifier.height(spacing.sm))
        SectionHeader(title = "Saved", overline = if (saved.size == 1) "1 guide" else "${saved.size} guides")
        Spacer(Modifier.height(12.dp))
        saved.forEach { article ->
            ArticleRow(article = article, saved = true, onOpen = { open(article) }, onToggleSave = { viewModel.toggleSaved(article.id) })
            Spacer(Modifier.height(12.dp))
        }
    }
    Spacer(Modifier.height(spacing.sm))
    InfoNotice(text = "General education, not medical advice. If something about your skin worries you, speak to a dermatologist.")
}

@Composable
private fun FeaturedArticleCard(article: LearningArticle, onOpen: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onOpen, contentPadding = PaddingValues(0.dp)) {
        ArticleArtwork(category = article.category, modifier = Modifier.fillMaxWidth().height(172.dp), seed = article.id.hashCode())
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tag(text = "Featured", tone = TagTone.BLUSH)
                Spacer(Modifier.width(8.dp))
                Tag(text = article.category.label, tone = TagTone.MIST)
                Spacer(Modifier.weight(1f))
                Text(text = "${article.readMinutes} min read", style = typography.caption, color = colors.textMuted)
            }
            Spacer(Modifier.height(10.dp))
            Text(text = article.title, style = typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(text = article.subtitle, style = typography.bodySmall, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(text = "By ${article.authorName}", style = typography.caption, color = colors.textMuted)
        }
    }
}

@Composable
private fun CompactArticleCard(article: LearningArticle, onOpen: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = Modifier.width(220.dp), onClick = onOpen, contentPadding = PaddingValues(0.dp)) {
        ArticleArtwork(category = article.category, modifier = Modifier.fillMaxWidth().height(108.dp), seed = article.id.hashCode())
        Column(Modifier.padding(14.dp)) {
            SectionOverline(text = article.category.label)
            Spacer(Modifier.height(4.dp))
            Text(text = article.title, style = typography.label, color = colors.textPrimary, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(text = "${article.readMinutes} min read", style = typography.caption, color = colors.textMuted)
        }
    }
}

@Composable
fun ArticleRow(article: LearningArticle, saved: Boolean, onOpen: () -> Unit, onToggleSave: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onOpen, contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ArticleArtwork(
                category = article.category,
                modifier = Modifier.size(72.dp).clip(SkinthesiaTheme.shapes.tile),
                seed = article.id.hashCode(),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                SectionOverline(text = article.category.label)
                Spacer(Modifier.height(2.dp))
                Text(text = article.title, style = typography.label, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${article.readMinutes} min · ${article.authorName}",
                    style = typography.caption,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconAction(
                icon = if (saved) SkinthesiaIcons.BookmarkFilled else SkinthesiaIcons.Bookmark,
                contentDescription = if (saved) "Remove from saved" else "Save guide",
                onClick = onToggleSave,
                tint = if (saved) colors.primary else colors.textMuted,
            )
        }
    }
}
