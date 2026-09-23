package com.phantompad.client.state

import com.phantompad.client.protocol.DiscoveryResponse

/**
 * Represents the app's connection lifecycle state machine.
 */
sealed class ConnectionState {
    /** Not connected, showing server discovery. */
    data class Disconnected(
        val servers: List<DiscoveryResponse> = emptyList(),
        val isScanning: Boolean = false,
    ) : ConnectionState()

    /** Attempting to connect to a server. */
    data class Connecting(
        val serverName: String,
        val address: String,
    ) : ConnectionState()

    /** Connected and controlling. */
    data class Connected(
        val serverName: String,
        val slotId: Int,
        val latencyMs: Long = 0,
    ) : ConnectionState()

    /** Connection failed or was rejected. */
    data class Error(
        val message: String,
    ) : ConnectionState()
}
