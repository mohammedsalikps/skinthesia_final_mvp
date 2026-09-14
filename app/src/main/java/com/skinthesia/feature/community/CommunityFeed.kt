package com.skinthesia.feature.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CommunityPostRoute
import com.skinthesia.core.navigation.CreatePostRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MonogramAvatar
import com.skinthesia.core.ui.components.ScrollableFilterTabs
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaSecondaryButton
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.core.ui.relativeTime
import com.skinthesia.domain.model.CommunityAuthor
import com.skinthesia.domain.model.CommunityPost
import com.skinthesia.domain.model.CommunitySection
import com.skinthesia.domain.repository.CommunityRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommunityFeedViewModel(
    private val community: CommunityRepository,
    profiles: UserProfileRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val _section = MutableStateFlow(CommunitySection.FOR_YOU)
    val section: StateFlow<CommunitySection> = _section.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val posts: StateFlow<List<CommunityPost>?> = combine(
        _section,
        profiles.profile.map { it.goals.goals.toSet() }.distinctUntilChanged(),
        settings.settings.map { it.personalizedContent }.distinctUntilChanged(),
    ) { section, goals, personal -> section to (if (personal) goals else emptySet()) }
        .flatMapLatest { (section, goals) -> community.feed(section, goals) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun select(section: CommunitySection) {
        _section.value = section
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch { community.toggleLike(postId) }
    }
}

/** The community feed, embedded in the Learn tab. Text-first, supportive, never comparative. */
@Composable
fun CommunityFeed() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { CommunityFeedViewModel(community, profiles, settings) }
    val section by viewModel.section.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val sections = CommunitySection.entries
    val now = remember { System.currentTimeMillis() }

    ScrollableFilterTabs(
        options = sections.map { it.label },
        selectedIndex = sections.indexOf(section),
        onSelect = { viewModel.select(sections[it]) },
        contentPadding = PaddingValues(0.dp),
    )
    Spacer(Modifier.height(12.dp))
    Text(text = section.description, style = typography.bodySmall, color = colors.textSecondary)
    Spacer(Modifier.height(spacing.md))
    SkinthesiaCard(containerColor = colors.successSoft, borderColor = Color.Transparent) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(SkinthesiaIcons.Shield, contentDescription = null, tint = colors.successStrong, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "A space for learning and support. Share experiences, not comparisons, and leave diagnoses to professionals.",
                style = typography.bodySmall,
                color = colors.successStrong,
            )
        }
    }
    Spacer(Modifier.height(spacing.md))
    SkinthesiaSecondaryButton(
        text = if (section == CommunitySection.QUESTIONS) "Ask a question" else "Ask or share",
        leadingIcon = SkinthesiaIcons.Plus,
        onClick = { navigator.navigate(CreatePostRoute(section.takeIf { it in CommunitySection.postable }?.name)) },
    )
    Spacer(Modifier.height(spacing.md))
    val list = posts
    when {
        list == null -> LoadingState(message = "Loading conversations")
        list.isEmpty() -> EmptyState(
            icon = SkinthesiaIcons.Community,
            title = "Nothing here yet",
            body = "Start the first conversation in ${section.label.lowercase()}.",
        )
        else -> list.forEach { post ->
            PostCard(
                post = post,
                now = now,
                onOpen = { navigator.navigate(CommunityPostRoute(post.id)) },
                onLike = { viewModel.toggleLike(post.id) },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(post: CommunityPost, now: Long, onOpen: () -> Unit, onLike: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onOpen) {
        AuthorRow(author = post.author, createdAt = post.createdAt, now = now)
        Spacer(Modifier.height(10.dp))
        if (post.isPinned) {
            Tag(text = "Pinned", tone = TagTone.NEUTRAL, icon = SkinthesiaIcons.Pin)
            Spacer(Modifier.height(8.dp))
        }
        Text(text = post.title, style = typography.titleSmall, color = colors.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(text = post.body, style = typography.bodySmall, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
        if (post.tags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                post.tags.take(3).forEach { Tag(text = it, tone = TagTone.MIST) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LikeButton(liked = post.likedByMe, count = post.likeCount, onClick = onLike)
            Spacer(Modifier.width(14.dp))
            Icon(SkinthesiaIcons.Comment, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (post.comments.size == 1) "1 reply" else "${post.comments.size} replies",
                style = typography.caption,
                color = colors.textSecondary,
            )
            Spacer(Modifier.weight(1f))
            if (post.hasExpertAnswer) Tag(text = "Expert answered", tone = TagTone.SAGE, icon = SkinthesiaIcons.Award)
        }
    }
}

@Composable
fun AuthorRow(author: CommunityAuthor, createdAt: Long, now: Long, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        MonogramAvatar(name = author.displayName, size = 34.dp, isExpert = author.isExpert)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = author.displayName + if (author.isCurrentUser) " (you)" else "",
                    style = typography.label,
                    color = colors.textPrimary,
                )
                if (author.isExpert) {
                    Spacer(Modifier.width(6.dp))
                    Icon(SkinthesiaIcons.Award, contentDescription = "Expert", tint = colors.primary, modifier = Modifier.size(13.dp))
                }
            }
            Text(
                text = listOfNotNull(author.credential, author.journeyWeek?.let { "Week $it of their journey" }, relativeTime(createdAt, now)).joinToString(" · "),
                style = typography.caption,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun LikeButton(liked: Boolean, count: Int, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .toggleable(value = liked, role = Role.Checkbox, onValueChange = { onClick() })
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (liked) SkinthesiaIcons.HeartFilled else SkinthesiaIcons.Heart,
            contentDescription = if (liked) "Helpful, selected" else "Mark as helpful",
            tint = if (liked) colors.accentBlushDeep else colors.textMuted,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(text = "$count", style = SkinthesiaTheme.typography.caption, color = colors.textSecondary)
    }
}
