package com.phantompad.client.network

import com.phantompad.client.protocol.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Main PhantomPad UDP client.
 *
 * Lifecycle: create -> connect() -> send input / ping -> disconnect() -> close()
 *
 * Runs a receive loop on a background coroutine that handles Pong,
 * HapticFeedback, and server-initiated Disconnect packets.
 */
class PhantomPadClient(
    private val hapticManager: HapticManager?,
) {
    private var socket: UdpSocket? = null
    private var serverAddress: String = ""
    private var serverPort: Int = SERVER_PORT

    private var receiveJob: Job? = null
    private var pingJob: Job? = null
    private var sequence: UInt = 0u

    private val _latencyMs = MutableStateFlow(0L)
    val latencyMs: StateFlow<Long> = _latencyMs.asStateFlow()

    private val _disconnected = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val disconnected: SharedFlow<String> = _disconnected.asSharedFlow()

    val isConnected: Boolean get() = socket?.isOpen == true

    /**
     * Connect to a server. Returns the ConnectResponse on success.
     * Throws on network error or connection rejection.
     */
    suspend fun connect(
        address: String,
        port: Int = SERVER_PORT,
        playerName: String,
        scope: CoroutineScope,
    ): ConnectResponse = withContext(Dispatchers.IO) {
        val sock = UdpSocket()
        sock.open()

        serverAddress = address
        serverPort = port

        val connectPacket = PacketEncoder.connectRequest(playerName)
        sock.sendTo(address, port, connectPacket)

        // Wait for ConnectResponse with timeout
        val datagram = sock.receive(timeoutMs = 5000)
            ?: throw Exception("Connection timed out")

        val response = PacketDecoder.connectResponse(datagram.data, datagram.size)
            ?: throw Exception("Invalid server response")

        if (response.status != ConnectStatus.Success) {
            sock.close()
            throw Exception(response.status.toMessage())
        }

        socket = sock
        sequence = 0u

        // Start receive loop
        receiveJob = scope.launch(Dispatchers.IO) {
            receiveLoop()
        }

        // Start ping loop (every 5 seconds)
        pingJob = scope.launch(Dispatchers.IO) {
            pingLoop()
        }

        response
    }

    /**
     * Send current controller state to the server.
     * Should be called on every input change (event-driven).
     */
    fun sendInput(state: ControllerState) {
        val sock = socket ?: return
        sequence++
        val packet = PacketEncoder.input(sequence, state)
        try {
            sock.sendTo(serverAddress, serverPort, packet)
        } catch (_: Exception) {
            // non-fatal, UDP is best-effort
        }
    }

    /**
     * Gracefully disconnect from the server.
     */
    fun disconnect() {
        receiveJob?.cancel()
        pingJob?.cancel()
        receiveJob = null
        pingJob = null

        try {
            val sock = socket
            if (sock != null && sock.isOpen) {
                val packet = PacketEncoder.disconnect()
                sock.sendTo(serverAddress, serverPort, packet)
            }
        } catch (_: Exception) {
            // best effort
        }

        socket?.close()
        socket = null
        sequence = 0u
    }

    /**
     * Background receive loop: handles Pong, HapticFeedback, Disconnect.
     */
    private suspend fun receiveLoop() {
        val buf = ByteArray(512)
        try {
            while (coroutineContext.isActive) {
                val sock = socket ?: break
                val datagram = sock.receive(timeoutMs = 1000) ?: continue

                val type = PacketDecoder.packetType(datagram.data, datagram.size) ?: continue

                when (type) {
                    PacketType.PONG -> {
                        val sentTimestamp = PacketDecoder.pong(datagram.data, datagram.size)
                        if (sentTimestamp != null) {
                            _latencyMs.value = currentTimeMillis() - sentTimestamp
                        }
                    }

                    PacketType.HAPTIC_FEEDBACK -> {
                        val feedback = PacketDecoder.hapticFeedback(datagram.data, datagram.size)
                        if (feedback != null) {
                            hapticManager?.vibrate(
                                feedback.strongMagnitude.toInt(),
                                feedback.weakMagnitude.toInt(),
                            )
                        }
                    }

                    PacketType.DISCONNECT -> {
                        _disconnected.tryEmit("Server disconnected")
                        disconnect()
                        break
                    }
                }
            }
        } catch (_: CancellationException) {
            // normal shutdown
        } catch (_: Exception) {
            _disconnected.tryEmit("Connection lost")
            disconnect()
        }
    }

    /**
     * Sends a ping every 5 seconds to keep the connection alive
     * and measure latency.
     */
    private suspend fun pingLoop() {
        try {
            while (coroutineContext.isActive) {
                delay(5000)
                val sock = socket ?: break
                val timestamp = currentTimeMillis()
                val packet = PacketEncoder.ping(timestamp)
                try {
                    sock.sendTo(serverAddress, serverPort, packet)
                } catch (_: Exception) {
                    // non-fatal
                }
            }
        } catch (_: CancellationException) {
            // normal
        }
    }
}
