package com.skinthesia.core.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.ContentMaxWidth
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/**
 * The standard Skinthesia page: warm background, safe-area handling, an optional
 * top bar, a centred reading column capped at [ContentMaxWidth], and an optional
 * pinned action area that respects the navigation bar and keyboard.
 */
@Composable
fun SkinthesiaScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    scrollable: Boolean = true,
    scrollState: ScrollState = rememberScrollState(),
    horizontalPadding: Dp = SkinthesiaTheme.spacing.screenHorizontal,
    background: Color = SkinthesiaTheme.colors.background,
    /** False inside the tab shell, where the bottom navigation already handles the inset. */
    insetBottom: Boolean = true,
    contentAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = SkinthesiaTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        topBar()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (scrollable) Modifier.verticalScroll(scrollState) else Modifier)
                    .padding(horizontal = horizontalPadding)
                    .then(if (bottomBar == null && insetBottom) Modifier.navigationBarsPadding() else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .then(if (scrollable) Modifier else Modifier.weight(1f)),
                    horizontalAlignment = contentAlignment,
                ) {
                    content()
                    if (scrollable) Spacer(Modifier.height(spacing.xl))
                }
            }
        }
        if (bottomBar != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to background.copy(alpha = 0f),
                            0.25f to background,
                            1f to background,
                        ),
                    )
                    .padding(horizontal = horizontalPadding)
                    .padding(top = spacing.sm)
                    .then(if (insetBottom) Modifier.navigationBarsPadding() else Modifier)
                    .imePadding()
                    .padding(bottom = spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = bottomBar,
                )
            }
        }
    }
}

/**
 * 56 dp bar with an optional back control, a centred serif title or the brand mark,
 * and trailing actions. Handles the status bar inset itself.
 */
@Composable
fun SkinthesiaTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    showBrand: Boolean = false,
    backIcon: ImageVector = SkinthesiaIcons.ArrowLeft,
    backDescription: String = "Back",
    containerColor: Color = Color.Transparent,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
    ) {
        if (onBack != null) {
            IconAction(
                icon = backIcon,
                contentDescription = backDescription,
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
        when {
            title != null -> Text(
                text = title,
                style = typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 104.dp)
                    .semantics { heading() },
            )
            showBrand -> Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandMonogram(size = 24.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Skinthesia",
                    style = typography.brand.copy(fontSize = 19.sp, lineHeight = 22.sp),
                    color = colors.primary,
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

/** 48 dp circular icon control with an optional count badge. */
@Composable
fun IconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = SkinthesiaTheme.colors.textPrimary,
    badgeCount: Int = 0,
    containerColor: Color = Color.Transparent,
) {
    val colors = SkinthesiaTheme.colors
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 8.dp)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    style = SkinthesiaTheme.typography.navigation.copy(fontSize = 9.sp),
                    color = colors.textOnPrimary,
                )
            }
        }
    }
}

/** Editorial page header: optional overline, serif title and supporting line. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    subtitle: String? = null,
    centered: Boolean = false,
    titleStyle: androidx.compose.ui.text.TextStyle = SkinthesiaTheme.typography.title,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val align = if (centered) TextAlign.Center else TextAlign.Start
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (overline != null) SectionOverline(text = overline)
        Text(
            text = title,
            style = titleStyle,
            color = colors.textPrimary,
            textAlign = align,
            modifier = Modifier.semantics { heading() },
        )
        if (subtitle != null) {
            Text(text = subtitle, style = typography.subtitle, color = colors.textSecondary, textAlign = align)
        }
    }
}
