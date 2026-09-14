package com.skinthesia.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** Development-only stand-in while a tab is assembled; replaced as each phase lands. */
@Composable
fun TabPlaceholder(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkinthesiaTheme.colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(icon = SkinthesiaIcons.Sparkle, title = title, body = "This space is being prepared.")
    }
}
