package com.skinthesia.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.components.FadeInUp

/**
 * Fixed-column grid of equally sized tiles that lives happily inside a
 * scrolling column, with each row cascading in.
 */
@Composable
fun <T> SelectionGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    content: @Composable (T) -> Unit,
) {
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.gridGap),
    ) {
        items.chunked(columns).forEachIndexed { rowIndex, row ->
            FadeInUp(delayMillis = motion.stagger(rowIndex + 1)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.gridGap),
                ) {
                    row.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) { content(item) }
                    }
                    repeat(columns - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
