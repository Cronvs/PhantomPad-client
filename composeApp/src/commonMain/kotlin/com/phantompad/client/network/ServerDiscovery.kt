package com.phantompad.client.network

import com.phantompad.client.protocol.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Discovers PhantomPad servers on the local network via UDP broadcast.
 * Sends DiscoveryRequest to 255.255.255.255:35700 and collects responses.
 */
class ServerDiscovery {

    private val _servers = MutableStateFlow<List<DiscoveryResponse>>(emptyList())
    val servers: StateFlow<List<DiscoveryResponse>> = _servers.asStateFlow()

    private var scanJob: Job? = null

    /**
     * Start scanning for servers. Runs continuous scans until stopped.
     * Each scan sends a broadcast and listens for responses for [scanWindowMs].
     * Rescans every [intervalMs].
     */
    fun startScanning(
        scope: CoroutineScope,
        scanWindowMs: Int = 2000,
        intervalMs: Long = 3000,
    ) {
        stopScanning()
        scanJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val found = performScan(scanWindowMs)
                _servers.value = found
                delay(intervalMs)
            }
        }
    }

    fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    /**
     * Perform a single scan cycle.
     */
    private fun performScan(scanWindowMs: Int): List<DiscoveryResponse> {
        val socket = UdpSocket()
        val results = mutableListOf<DiscoveryResponse>()

        try {
            socket.open()
            socket.enableBroadcast()

            val timestamp = currentTimeMillis()
            val packet = PacketEncoder.discoveryRequest(timestamp)
            socket.sendTo("255.255.255.255", SERVER_PORT, packet)

            val deadline = currentTimeMillis() + scanWindowMs
            while (true) {
                val remaining = (deadline - currentTimeMillis()).toInt()
                if (remaining <= 0) break

                val datagram = socket.receive(timeoutMs = remaining) ?: break
                val response = PacketDecoder.discoveryResponse(datagram.data, datagram.size)
                if (response != null) {
                    val latency = currentTimeMillis() - response.timestamp
                    results.add(
                        response.copy(
                            address = datagram.address,
                            port = datagram.port,
                            latencyMs = latency.coerceAtLeast(0),
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // scan failure is non-fatal
        } finally {
            socket.close()
        }

        return results
    }
}

internal expect fun currentTimeMillis(): Long
