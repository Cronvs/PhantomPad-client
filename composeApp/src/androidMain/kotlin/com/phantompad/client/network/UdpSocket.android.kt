package com.phantompad.client.network

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

actual class UdpSocket actual constructor() {
    private var socket: DatagramSocket? = null

    actual fun open() {
        socket = DatagramSocket().also {
            it.reuseAddress = true
        }
    }

    actual fun enableBroadcast() {
        socket?.broadcast = true
    }

    actual fun sendTo(address: String, port: Int, data: ByteArray) {
        val sock = socket ?: return
        val addr = InetAddress.getByName(address)
        val packet = DatagramPacket(data, data.size, addr, port)
        sock.send(packet)
    }

    actual fun receive(timeoutMs: Int): UdpDatagram? {
        val sock = socket ?: return null
        val buf = ByteArray(65536)
        val packet = DatagramPacket(buf, buf.size)
        return try {
            sock.soTimeout = timeoutMs
            sock.receive(packet)
            UdpDatagram(
                data = buf,
                size = packet.length,
                address = packet.address.hostAddress ?: "",
                port = packet.port,
            )
        } catch (_: SocketTimeoutException) {
            null
        }
    }

    actual fun close() {
        socket?.close()
        socket = null
    }

    actual val isOpen: Boolean get() = socket != null && !socket!!.isClosed
}

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()
