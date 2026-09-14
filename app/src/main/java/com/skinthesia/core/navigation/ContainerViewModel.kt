package com.skinthesia.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skinthesia.AppContainer
import com.skinthesia.LocalAppContainer

/**
 * Creates a ViewModel scoped to the current navigation entry, built from the
 * [AppContainer] with the entry's SavedStateHandle (which carries route arguments).
 */
@Composable
inline fun <reified VM : ViewModel> containerViewModel(
    key: String? = null,
    crossinline create: AppContainer.(SavedStateHandle) -> VM,
): VM {
    val container = LocalAppContainer.current
    val factory = remember(container) {
        viewModelFactory { initializer { container.create(createSavedStateHandle()) } }
    }
    return viewModel(key = key, factory = factory)
}
