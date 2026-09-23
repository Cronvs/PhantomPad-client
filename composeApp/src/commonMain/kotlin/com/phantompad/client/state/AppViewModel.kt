package com.phantompad.client.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantompad.client.network.HapticManager
import com.phantompad.client.network.PhantomPadClient
import com.phantompad.client.network.ServerDiscovery
import com.phantompad.client.protocol.ConnectStatus
import com.phantompad.client.protocol.DiscoveryResponse
import com.phantompad.client.protocol.SERVER_PORT
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AppViewModel(
    hapticManager: HapticManager? = null,
) : ViewModel() {

    private val discovery = ServerDiscovery()
    private val client = PhantomPadClient(hapticManager)
    val controllerInput = ControllerInput()

    private val _connectionState = MutableStateFlow<ConnectionState>(
        ConnectionState.Disconnected()
    )
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _playerName = MutableStateFlow("Player")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    init {
        // Observe discovered servers and update state
        viewModelScope.launch {
            discovery.servers.collect { servers ->
                val current = _connectionState.value
                if (current is ConnectionState.Disconnected) {
                    _connectionState.value = current.copy(
                        servers = servers,
                        isScanning = true,
                    )
                }
            }
        }

        // Observe latency while connected
        viewModelScope.launch {
            client.latencyMs.collect { latency ->
                val current = _connectionState.value
                if (current is ConnectionState.Connected) {
                    _connectionState.value = current.copy(latencyMs = latency)
                }
            }
        }

        // Observe server-initiated disconnects
        viewModelScope.launch {
            client.disconnected.collect { reason ->
                _connectionState.value = ConnectionState.Error(reason)
            }
        }
    }

    fun setPlayerName(name: String) {
        _playerName.value = name.take(30)
    }

    fun startDiscovery() {
        discovery.startScanning(viewModelScope)
        val current = _connectionState.value
        if (current is ConnectionState.Disconnected) {
            _connectionState.value = current.copy(isScanning = true)
        }
    }

    fun stopDiscovery() {
        discovery.stopScanning()
    }

    fun connectToServer(server: DiscoveryResponse) {
        connectToAddress(server.address, SERVER_PORT, server.name)
    }

    fun connectToAddress(address: String, port: Int = SERVER_PORT, displayName: String = address) {
        _connectionState.value = ConnectionState.Connecting(
            serverName = displayName,
            address = address,
        )
        discovery.stopScanning()

        viewModelScope.launch {
            try {
                val response = client.connect(
                    address = address,
                    port = port,
                    playerName = _playerName.value,
                    scope = viewModelScope,
                )
                _connectionState.value = ConnectionState.Connected(
                    serverName = response.serverName,
                    slotId = response.slotId,
                )
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(
                    message = e.message ?: "Connection failed"
                )
            }
        }
    }

    fun disconnect() {
        client.disconnect()
        controllerInput.reset()
        _connectionState.value = ConnectionState.Disconnected()
    }

    fun dismissError() {
        _connectionState.value = ConnectionState.Disconnected()
    }

    /**
     * Called by the controller UI whenever input changes.
     * Converts the current input state and sends it to the server.
     */
    fun sendInput() {
        if (_connectionState.value is ConnectionState.Connected) {
            client.sendInput(controllerInput.toControllerState())
        }
    }

    override fun onCleared() {
        super.onCleared()
        discovery.stopScanning()
        client.disconnect()
    }
}
