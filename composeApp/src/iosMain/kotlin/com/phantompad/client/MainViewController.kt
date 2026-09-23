package com.phantompad.client

import androidx.compose.ui.window.ComposeUIViewController
import com.phantompad.client.network.HapticManager
import com.phantompad.client.state.AppViewModel
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val hapticManager = HapticManager()
    val viewModel = AppViewModel(hapticManager)

    // Keep screen on
    UIApplication.sharedApplication.idleTimerDisabled = true

    return ComposeUIViewController {
        App(viewModel)
    }
}
