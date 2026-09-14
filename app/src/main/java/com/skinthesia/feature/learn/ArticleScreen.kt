package com.skinthesia.feature.learn

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.ArticleRoute
import com.skinthesia.core.navigation.ExpertProfileRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.ArticleArtwork
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.IconAction
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MonogramAvatar
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPillButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatDay
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.ArticleBlock
import com.skinthesia.domain.model.LearningArticle
import com.skinthesia.domain.repository.LearningRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ArticleUiState(
    val loading: Boolean = true,
    val article: LearningArticle? = null,
    val related: List<LearningArticle> = emptyList(),
    val saved: Boolean = false,
    val savedIds: Set<String> = emptySet(),
)

class ArticleViewModel(val articleId: String, private val learning: LearningRepository) : ViewModel() {
    val state: StateFlow<ArticleUiState> = combine(learning.articles, learning.savedIds) { articles, saved ->
        val article = articles.firstOrNull { it.id == articleId }
        ArticleUiState(
            loading = false,
            article = article,
            related = article?.relatedArticleIds?.mapNotNull { id -> articles.firstOrNull { it.id == id } }.orEmpty(),
            saved = articleId in saved,
            savedIds = saved,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArticleUiState())

    fun toggleSaved(id: String = articleId) {
        viewModelScope.launch { learning.toggleSaved(id) }
    }
}

/** An article, rendered with the editorial typography: lead, headings, callouts and takeaways. */
@Composable
fun ArticleScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> ArticleViewModel(handle.toRoute<ArticleRoute>().articleId, learning) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing

    SkinthesiaScreen(
        topBar = {
            SkinthesiaTopBar(
                onBack = navigator::back,
                actions = {
                    if (state.article != null) {
                        IconAction(
                            icon = if (state.saved) SkinthesiaIcons.BookmarkFilled else SkinthesiaIcons.Bookmark,
                            contentDescription = if (state.saved) "Remove from saved" else "Save guide",
                            onClick = { viewModel.toggleSaved() },
                            tint = if (state.saved) colors.primary else colors.textPrimary,
                        )
                    }
                },
            )
        },
    ) {
        val article = state.article
        when {
            state.loading -> LoadingState(message = "Opening guide")
            article == null -> ScreenHeader(title = "Guide not found", centered = true)
            else -> {
                FadeInUp {
                    ArticleArtwork(
                        category = article.category,
                        modifier = Modifier.fillMaxWidth().height(210.dp).clip(SkinthesiaTheme.shapes.card),
                        seed = article.id.hashCode(),
                    )
                }
                Spacer(Modifier.height(spacing.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Tag(text = article.category.label, tone = TagTone.MIST)
                    Spacer(Modifier.width(10.dp))
                    val published = runCatching { formatDayOf(LocalDate.parse(article.publishedOn)) }.getOrDefault(article.publishedOn)
                    Text(text = "${article.readMinutes} min read · $published", style = typography.caption, color = colors.textMuted)
                }
                Spacer(Modifier.height(12.dp))
                Text(text = article.title, style = typography.display, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(8.dp))
                Text(text = article.subtitle, style = typography.articleLead, color = colors.textSecondary)
                Spacer(Modifier.height(spacing.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonogramAvatar(name = article.authorName, size = 40.dp, isExpert = article.expertId != null)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(text = article.authorName, style = typography.label, color = colors.textPrimary)
                        Text(text = article.authorRole, style = typography.caption, color = colors.textMuted)
                    }
                }
                Spacer(Modifier.height(spacing.md))
                SkinthesiaDivider()
                Spacer(Modifier.height(spacing.md))
                article.blocks.forEach { block ->
                    ArticleBlockView(block)
                    Spacer(Modifier.height(14.dp))
                }
                article.expertId?.let { expertId ->
                    Spacer(Modifier.height(spacing.sm))
                    SkinthesiaCard(containerColor = colors.blushMist, borderColor = Color.Transparent) {
                        Text(text = "Questions about this topic?", style = typography.labelLarge, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(text = "${article.authorName} offers consultations through Skinthesia.", style = typography.bodySmall, color = colors.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        SkinthesiaPillButton(text = "View profile", onClick = { navigator.navigate(ExpertProfileRoute(expertId)) })
                    }
                }
                if (state.related.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.lg))
                    SectionHeader(title = "Keep reading", overline = "Related guides")
                    Spacer(Modifier.height(12.dp))
                    state.related.forEach { related ->
                        ArticleRow(
                            article = related,
                            saved = related.id in state.savedIds,
                            onOpen = { navigator.push(ArticleRoute(related.id)) },
                            onToggleSave = { viewModel.toggleSaved(related.id) },
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
                Spacer(Modifier.height(spacing.sm))
                InfoNotice(text = "General education, not medical advice. Speak to a dermatologist about anything that worries you.")
            }
        }
    }
}

private fun formatDayOf(date: LocalDate): String = formatDay(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())

@Composable
private fun ArticleBlockView(block: ArticleBlock) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    when (block) {
        is ArticleBlock.Paragraph -> Text(text = block.text, style = typography.articleBody, color = colors.textPrimary)
        is ArticleBlock.Heading -> Text(
            text = block.text,
            style = typography.titleSmall,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 6.dp).semantics { heading() },
        )
        is ArticleBlock.Callout -> SkinthesiaCard(containerColor = colors.blushMist, borderColor = Color.Transparent) {
            Text(text = block.title, style = typography.labelLarge, color = colors.primary)
            Spacer(Modifier.height(4.dp))
            Text(text = block.text, style = typography.body, color = colors.textSecondary)
        }
        is ArticleBlock.Bullets -> Column {
            block.items.forEach { item ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 10.dp).size(5.dp).clip(CircleShape).background(colors.primary))
                    Spacer(Modifier.width(12.dp))
                    Text(text = item, style = typography.articleBody, color = colors.textPrimary)
                }
            }
        }
        is ArticleBlock.Takeaways -> SkinthesiaCard {
            SectionOverline(text = "Key takeaways")
            Spacer(Modifier.height(8.dp))
            block.items.forEach { item ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(text = item, style = typography.body, color = colors.textPrimary)
                }
            }
        }
    }
}
