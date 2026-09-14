package com.skinthesia.core.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Content never stretches wider than this, so large phones, foldables and
 * tablets keep the phone-like editorial rhythm with generous side margins.
 * Height-sensitive layouts (such as Welcome) adapt with BoxWithConstraints.
 */
val ContentMaxWidth: Dp = 560.dp
