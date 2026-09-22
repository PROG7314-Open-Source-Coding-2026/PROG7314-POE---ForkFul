package com.emeris.forkful.ui.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emeris.forkful.AppContainer
import com.emeris.forkful.ForkfulApplication

@Composable
inline fun <reified VM : ViewModel> containerViewModel(
    crossinline create: (AppContainer) -> VM
): VM {
    val container = (LocalContext.current.applicationContext as ForkfulApplication).container
    return viewModel<VM>(
        factory = viewModelFactory {
            initializer { create(container) }
        }
    )
}
