package com.emeris.forkful.ui.di

/**
 * Forkful - ViewModel Dependency Injection Factory Utilities
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Provides composition-scoped helper functions for injecting AppContainer dependencies into Jetpack Compose ViewModels.
 */

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

//------------------------------End-Of-File------------------------------//
