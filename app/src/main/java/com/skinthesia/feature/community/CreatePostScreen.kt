package com.skinthesia.feature.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CommunityPostRoute
import com.skinthesia.core.navigation.CreatePostRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FilterPill
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.CommunitySection
import com.skinthesia.domain.repository.CommunityRepository
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePostUiState(
    val section: CommunitySection = CommunitySection.QUESTIONS,
    val title: String = "",
    val body: String = "",
    val tags: Set<String> = emptySet(),
    val availableTags: List<String> = emptyList(),
    val posting: Boolean = false,
) {
    val canPost: Boolean get() = title.trim().length >= 4 && body.trim().length >= 10 && !posting
}

class CreatePostViewModel(
    initialSection: CommunitySection,
    private val community: CommunityRepository,
    profiles: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostUiState(section = initialSection))
    val state: StateFlow<CreatePostUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val goals = profiles.current().goals.goals.map { it.label }
            _state.update { it.copy(availableTags = (goals + listOf("Routine", "Products", "Sunscreen", "Sensitive skin")).distinct()) }
        }
    }

    fun setSection(section: CommunitySection) = _state.update { it.copy(section = section) }
    fun setTitle(value: String) = _state.update { it.copy(title = value.take(120)) }
    fun setBody(value: String) = _state.update { it.copy(body = value.take(2_000)) }
    fun toggleTag(tag: String) = _state.update { s ->
        s.copy(tags = if (tag in s.tags) s.tags - tag else if (s.tags.size < 3) s.tags + tag else s.tags)
    }

    fun post(onPosted: (String) -> Unit) {
        val s = _state.value
        if (!s.canPost) return
        _state.update { it.copy(posting = true) }
        viewModelScope.launch {
            val post = community.createPost(s.section, s.title.trim(), s.body.trim(), s.tags.toList())
            _state.update { it.copy(posting = false) }
            onPosted(post.id)
        }
    }
}

/** Compose a community post. Text only, by design. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val requested = handle.toRoute<CreatePostRoute>().section?.let { s -> runCatching { CommunitySection.valueOf(s) }.getOrNull() }
        val section = requested?.takeIf { it in CommunitySection.postable } ?: CommunitySection.QUESTIONS
        CreatePostViewModel(section, community, profiles)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "New post", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = "Post",
                enabled = state.canPost,
                loading = state.posting,
                onClick = { viewModel.post { id -> navigator.replace(CommunityPostRoute(id)) } },
            )
        },
    ) {
        SectionOverline(text = "Where")
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CommunitySection.postable.forEach { section ->
                FilterPill(label = section.label, selected = state.section == section, onClick = { viewModel.setSection(section) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = state.section.description, style = typography.caption, color = colors.textMuted)
        Spacer(Modifier.height(spacing.lg))
        SkinthesiaTextField(
            value = state.title,
            onValueChange = viewModel::setTitle,
            label = "Title",
            placeholder = if (state.section == CommunitySection.QUESTIONS) "What would you like to ask?" else "Give it a short title",
            maxLength = 120,
        )
        Spacer(Modifier.height(12.dp))
        SkinthesiaTextField(
            value = state.body,
            onValueChange = viewModel::setBody,
            label = "Details",
            placeholder = "Add context: your skin type, what you've tried and what you noticed.",
            singleLine = false,
            minLines = 5,
            maxLength = 2_000,
        )
        Spacer(Modifier.height(spacing.md))
        SectionOverline(text = "Tags · up to three")
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.availableTags.forEach { tag ->
                FilterPill(label = tag, selected = tag in state.tags, onClick = { viewModel.toggleTag(tag) })
            }
        }
        Spacer(Modifier.height(spacing.lg))
        SkinthesiaCard(containerColor = colors.successSoft, borderColor = Color.Transparent) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(SkinthesiaIcons.Shield, contentDescription = null, tint = colors.successStrong, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Text only: this community is for learning, not comparing faces, so photos aren't supported. " +
                        "Please don't share personal medical details or diagnose others.",
                    style = typography.bodySmall,
                    color = colors.successStrong,
                )
            }
        }
    }
}
