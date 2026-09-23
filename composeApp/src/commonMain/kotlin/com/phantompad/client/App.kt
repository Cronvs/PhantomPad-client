package com.phantompad.client

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phantompad.client.state.AppViewModel
import com.phantompad.client.state.ConnectionState
import com.phantompad.client.ui.screens.ConnectionScreen
import com.phantompad.client.ui.screens.ControllerScreen
import com.phantompad.client.ui.theme.PhantomPadTheme

@Composable
fun App(viewModel: AppViewModel) {
    PhantomPadTheme {
        val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()

        AnimatedContent(
            targetState = connectionState is ConnectionState.Connected,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { isConnected ->
            if (isConnected) {
                val state = connectionState as ConnectionState.Connected
                ControllerScreen(
                    serverName = state.serverName,
                    slotId = state.slotId,
                    latencyMs = state.latencyMs,
                    controllerInput = viewModel.controllerInput,
                    onInputChanged = { viewModel.sendInput() },
                    onDisconnect = { viewModel.disconnect() },
                )
            } else {
                ConnectionScreen(
                    connectionState = connectionState,
                    playerName = viewModel.playerName.collectAsStateWithLifecycle().value,
                    onPlayerNameChange = viewModel::setPlayerName,
                    onStartDiscovery = viewModel::startDiscovery,
                    onStopDiscovery = viewModel::stopDiscovery,
                    onConnectToServer = viewModel::connectToServer,
                    onConnectToAddress = { address -> viewModel.connectToAddress(address) },
                    onDismissError = viewModel::dismissError,
                )
            }
        }
    }
}
