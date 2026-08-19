package com.seashore.zoonzoon

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController(
    configure = { opaque = false }
) { App() }