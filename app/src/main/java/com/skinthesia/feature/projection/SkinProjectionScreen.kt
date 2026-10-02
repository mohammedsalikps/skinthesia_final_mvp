package com.skinthesia.feature.projection

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.ai.projection.DemoProjectionProvider
import com.skinthesia.ai.projection.ProjectionFrame
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScrollableFilterTabs
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.core.ui.projection.toColorFilter
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SkinProjectionUiState(
    val loading: Boolean = true,
    val photoPath: String? = null,
    val horizonWeeks: Int = 12,
)

class SkinProjectionViewModel(
    assessments: AssessmentRepository,
    private val profiles: UserProfileRepository,
) : ViewModel() {

    private val profile = MutableStateFlow(UserProfile())

    init {
        viewModelScope.launch { profile.value = profiles.current() }
    }

    val state: StateFlow<SkinProjectionUiState> = combine(assessments.completed(), profile) { completed, p ->
        val latest = completed.lastOrNull { it.photo != null }
        SkinProjectionUiState(
            loading = false,
            photoPath = latest?.photo?.filePath,
            horizonWeeks = p.goals.durationWeeks.coerceAtLeast(1),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SkinProjectionUiState())
}

/**
 * A dedicated, illustrative "if you follow your plan" visual - never a guarantee.
 * Week 0 (displayed "Week 1") always shows the user's own real baseline photo
 * unfiltered; every later week compares it, via a drag handle, against the same
 * photo with a purely presentational colour lift from [DemoProjectionProvider] -
 * the same demo engine PotentialScreen's compact card already uses. Swapping in
 * a real generative model later only means implementing a new `ProjectionProvider`;
 * nothing here needs to change.
 */
@Composable
fun SkinProjectionScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { SkinProjectionViewModel(assessments, profiles) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    val typography = SkinthesiaTheme.typography
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion

    val provider = remember(state.horizonWeeks) { DemoProjectionProvider(state.horizonWeeks) }
    var week by rememberSaveable(state.horizonWeeks) { mutableIntStateOf(0) }

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = "Skin Projection", onBack = navigator::back) }) {
        when {
            state.loading -> LoadingState(message = "Loading your journey")
            state.photoPath == null -> EmptyState(
                icon = SkinthesiaIcons.SkinPrint,
                title = "Your projection appears after your first analysis",
                body = "Complete a selfie analysis to see your illustrative skin journey.",
            )
            else -> {
                FadeInUp {
                    Text(text = "See your potential skin journey.", style = typography.articleLead, color = colors.primary)
                }
                Spacer(Modifier.height(spacing.lg))
                val weekLabels = provider.availableWeeks.map { if (it == 0) "Week 1" else "Week $it" }
                FadeInUp(delayMillis = motion.stagger(1)) {
                    ScrollableFilterTabs(
                        options = weekLabels,
                        selectedIndex = provider.availableWeeks.indexOf(week).coerceAtLeast(0),
                        onSelect = { index -> week = provider.availableWeeks[index] },
                        contentPadding = PaddingValues(0.dp),
                    )
                }
                Spacer(Modifier.height(spacing.lg))
                FadeInUp(delayMillis = motion.stagger(2)) {
                    ProjectionComparisonCard(photoPath = state.photoPath!!, frame = provider.frame(week))
                }
                Spacer(Modifier.height(spacing.md))
                FadeInUp(delayMillis = motion.stagger(3)) {
                    Text(
                        text = "Based on your current SkinPrint™, personalized routine and consistency.",
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                Spacer(Modifier.height(spacing.lg))
                InfoNotice(
                    text = "This is an illustrative concept projection, not a medical prediction or a guaranteed " +
                        "outcome. If you consistently follow your personalized Skinthesia plan, this shows one " +
                        "example of how your skin may improve.",
                )
                Spacer(Modifier.height(spacing.xl))
            }
        }
    }
}

private fun weekTitle(frame: ProjectionFrame): String =
    if (frame.isBaseline) "Week 1" else frame.label.substringBefore(" ·")

@Composable
private fun ProjectionComparisonCard(photoPath: String, frame: ProjectionFrame) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(elevated = true) {
        SectionOverline(text = "Your skin journey")
        Spacer(Modifier.height(10.dp))
        if (frame.isBaseline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(SkinthesiaTheme.shapes.image)
                    .background(colors.surfaceMuted),
            ) {
                SkinthesiaImage(
                    source = ImageSource.LocalFile(photoPath),
                    contentDescription = "Your current photo",
                    modifier = Modifier.fillMaxSize(),
                    maxDimension = 1000,
                )
                PhotoLabel(text = "CURRENT · ${weekTitle(frame)}", modifier = Modifier.align(Alignment.BottomStart))
            }
        } else {
            BeforeAfterSlider(
                photoPath = photoPath,
                frame = frame,
                modifier = Modifier.fillMaxWidth().height(360.dp).clip(SkinthesiaTheme.shapes.image),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (frame.isBaseline) "Your actual photo, unedited." else "Drag to compare your current photo against the illustrative projection.",
            style = typography.caption,
            color = colors.textMuted,
        )
    }
}

/**
 * A draggable before/after comparison: the real, unfiltered photo underneath the
 * same photo under the demo projection's colour lift, with the top layer clipped
 * to a drag-controlled fraction - so the user always understands both sides are
 * the same real image, not two different photos.
 */
@Composable
private fun BeforeAfterSlider(photoPath: String, frame: ProjectionFrame, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    var fraction by remember(frame.week) { mutableFloatStateOf(0.5f) }
    BoxWithConstraints(
        modifier = modifier
            .background(colors.surfaceMuted)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    fraction = (fraction + dragAmount / size.width).coerceIn(0.03f, 0.97f)
                }
            }
            .clearAndSetSemantics {
                contentDescription = "Comparison: your current photo versus an illustrative projection for ${weekTitle(frame)}"
            },
    ) {
        val handleX = maxWidth * fraction

        // Projected (after) - full image, bottom layer.
        SkinthesiaImage(
            source = ImageSource.LocalFile(photoPath),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            maxDimension = 1000,
            colorFilter = frame.toColorFilter(),
        )
        // Current (before) - same image, unfiltered, clipped to the left `fraction` of the width.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent { clipRect(right = size.width * fraction) { this@drawWithContent.drawContent() } },
        ) {
            SkinthesiaImage(
                source = ImageSource.LocalFile(photoPath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                maxDimension = 1000,
            )
        }
        PhotoLabel(text = "CURRENT", modifier = Modifier.align(Alignment.BottomStart))
        PhotoLabel(text = "ILLUSTRATIVE PROJECTION · ${weekTitle(frame)}", modifier = Modifier.align(Alignment.BottomEnd))

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .offset(x = handleX)
                .background(Color.White.copy(alpha = 0.9f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = handleX - 18.dp)
                .size(36.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun PhotoLabel(text: String, modifier: Modifier = Modifier) {
    val typography = SkinthesiaTheme.typography
    Box(
        modifier = modifier
            .padding(10.dp)
            .clip(SkinthesiaTheme.shapes.pill)
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text = text, style = typography.overline, color = Color.White)
    }
}
