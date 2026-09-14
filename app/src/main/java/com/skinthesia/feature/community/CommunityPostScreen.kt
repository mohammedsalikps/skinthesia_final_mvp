package com.skinthesia.feature.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CommunityPostRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.CommunityComment
import com.skinthesia.domain.model.CommunityPost
import com.skinthesia.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Loading, missing, or loaded. */
data class PostUiState(val loading: Boolean = true, val post: CommunityPost? = null)

class CommunityPostViewModel(val postId: String, private val community: CommunityRepository) : ViewModel() {

    val state: StateFlow<PostUiState> = community.post(postId)
        .map { PostUiState(loading = false, post = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PostUiState())

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()
    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    fun setDraft(value: String) {
        _draft.value = value.take(1_000)
    }

    fun send() {
        val body = _draft.value.trim()
        if (body.isEmpty() || _sending.value) return
        _sending.value = true
        viewModelScope.launch {
            community.addComment(postId, body)
            _draft.value = ""
            _sending.value = false
        }
    }

    fun toggleLike() {
        viewModelScope.launch { community.toggleLike(postId) }
    }

    fun toggleCommentLike(id: String) {
        viewModelScope.launch { community.toggleCommentLike(id) }
    }
}

/** A post with its replies; expert answers are highlighted and listed first. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityPostScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> CommunityPostViewModel(handle.toRoute<CommunityPostRoute>().postId, community) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val now = remember { System.currentTimeMillis() }
    val post = state.post

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = post?.section?.label ?: "Community", onBack = navigator::back) },
        bottomBar = if (post != null) {
            {
                Row(verticalAlignment = Alignment.Bottom) {
                    SkinthesiaTextField(
                        value = draft,
                        onValueChange = viewModel::setDraft,
                        placeholder = "Add a kind, helpful reply",
                        singleLine = false,
                        maxLength = 1_000,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(10.dp))
                    val canSend = draft.isNotBlank() && !sending
                    Box(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (canSend) colors.primary else colors.disabledContainer)
                            .clickable(enabled = canSend, role = Role.Button, onClickLabel = "Send reply", onClick = viewModel::send),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (sending) {
                            CircularProgressIndicator(color = colors.textOnPrimary, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(SkinthesiaIcons.ArrowUpRight, contentDescription = "Send reply", tint = if (canSend) colors.textOnPrimary else colors.disabledContent)
                        }
                    }
                }
            }
        } else {
            null
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Loading conversation")
            post == null -> ScreenHeader(title = "Post not found", subtitle = "It may have been removed.", centered = true)
            else -> {
                AuthorRow(author = post.author, createdAt = post.createdAt, now = now)
                Spacer(Modifier.height(spacing.md))
                Text(text = post.title, style = typography.title, color = colors.textPrimary)
                Spacer(Modifier.height(10.dp))
                Text(text = post.body, style = typography.articleBody, color = colors.textPrimary)
                if (post.tags.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        post.tags.forEach { Tag(text = it, tone = TagTone.MIST) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LikeButton(liked = post.likedByMe, count = post.likeCount, onClick = viewModel::toggleLike)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "found this helpful", style = typography.caption, color = colors.textMuted)
                }
                Spacer(Modifier.height(spacing.md))
                SkinthesiaDivider()
                Spacer(Modifier.height(spacing.md))
                SectionOverline(text = if (post.comments.size == 1) "1 reply" else "${post.comments.size} replies")
                Spacer(Modifier.height(8.dp))
                if (post.comments.isEmpty()) {
                    Text(text = "No replies yet. Share what's worked for you.", style = typography.body, color = colors.textSecondary)
                }
                post.comments
                    .sortedWith(compareByDescending<CommunityComment> { it.isExpertAnswer }.thenBy { it.createdAt })
                    .forEach { comment ->
                        CommentItem(comment = comment, now = now, onLike = { viewModel.toggleCommentLike(comment.id) })
                        Spacer(Modifier.height(10.dp))
                    }
                Spacer(Modifier.height(spacing.md))
                Text(
                    text = "Replies share experience, not diagnoses. Expert answers are general guidance.",
                    style = typography.caption,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun CommentItem(comment: CommunityComment, now: Long, onLike: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val content: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth()) {
            if (comment.isExpertAnswer) {
                Tag(text = "Expert answer", tone = TagTone.SAGE, icon = SkinthesiaIcons.Award)
                Spacer(Modifier.height(8.dp))
            }
            AuthorRow(author = comment.author, createdAt = comment.createdAt, now = now)
            Spacer(Modifier.height(6.dp))
            Text(text = comment.body, style = typography.body, color = colors.textPrimary, modifier = Modifier.padding(start = 44.dp))
            Row(Modifier.padding(start = 38.dp, top = 4.dp)) {
                LikeButton(liked = comment.likedByMe, count = comment.likeCount, onClick = onLike)
            }
        }
    }
    if (comment.isExpertAnswer) {
        SkinthesiaCard(containerColor = colors.successSoft.copy(alpha = 0.5f), borderColor = Color.Transparent) { content() }
    } else {
        Box(Modifier.padding(vertical = 4.dp)) { content() }
    }
}
