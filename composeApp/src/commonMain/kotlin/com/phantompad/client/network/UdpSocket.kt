package com.phantompad.client.network

/**
 * Platform-abstracted UDP socket for PhantomPad communication.
 * Implementations use native platform APIs (DatagramSocket on Android,
 * POSIX/NWConnection on iOS) for reliable broadcast + unicast UDP.
 */
expect class UdpSocket() {
    /** Bind to a random local port. Must be called before send/receive. */
    fun open()

    /** Enable broadcast (SO_BROADCAST). Call after open(). */
    fun enableBroadcast()

    /** Send data to a specific address:port. */
    fun sendTo(address: String, port: Int, data: ByteArray)

    /**
     * Receive a single UDP datagram. Blocks until data arrives or timeout.
     * Returns null on timeout.
     * @param timeoutMs Receive timeout in milliseconds. 0 = infinite.
     */
    fun receive(timeoutMs: Int = 0): UdpDatagram?

    /** Close the socket and release resources. */
    fun close()

    /** Whether the socket is currently open. */
    val isOpen: Boolean
}

data class UdpDatagram(
    val data: ByteArray,
    val size: Int,
    val address: String,
    val port: Int,
)
