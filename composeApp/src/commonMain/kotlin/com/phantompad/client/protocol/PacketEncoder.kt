package com.phantompad.client.protocol

/**
 * Encodes PhantomPad packets into ByteArray for UDP transmission.
 * All multi-byte values are little-endian to match the C++ server's x86 layout.
 */
object PacketEncoder {

    /**
     * DiscoveryRequest: type(1) + timestamp(8) = 9 bytes
     */
    fun discoveryRequest(timestamp: Long): ByteArray {
        val buf = ByteArray(9)
        buf[0] = PacketType.DISCOVERY_REQUEST
        putLongLE(buf, 1, timestamp)
        return buf
    }

    /**
     * ConnectRequest: type(1) + clientVersion(2) + nameLength(1) + name(N)
     */
    fun connectRequest(playerName: String): ByteArray {
        val nameBytes = playerName.encodeToByteArray()
        val nameLen = nameBytes.size.coerceAtMost(255)
        val buf = ByteArray(4 + nameLen)
        buf[0] = PacketType.CONNECT_REQUEST
        putUShortLE(buf, 1, PROTOCOL_VERSION)
        buf[3] = nameLen.toByte()
        nameBytes.copyInto(buf, 4, 0, nameLen)
        return buf
    }

    /**
     * Disconnect: type(1) = 1 byte
     */
    fun disconnect(): ByteArray {
        return byteArrayOf(PacketType.DISCONNECT)
    }

    /**
     * Ping: type(1) + timestamp(8) = 9 bytes
     */
    fun ping(timestamp: Long): ByteArray {
        val buf = ByteArray(9)
        buf[0] = PacketType.PING
        putLongLE(buf, 1, timestamp)
        return buf
    }

    /**
     * Input: type(1) + sequence(4) + ControllerState(18) = 23 bytes
     */
    fun input(sequence: UInt, state: ControllerState): ByteArray {
        val buf = ByteArray(23)
        buf[0] = PacketType.INPUT
        putUIntLE(buf, 1, sequence)
        encodeControllerState(buf, 5, state)
        return buf
    }

    // -- ControllerState encoding (18 bytes) --
    // Field order must match C++ struct exactly:
    //   buttons(2) + leftThumbX(2) + leftThumbY(2) + rightThumbX(2) + rightThumbY(2)
    //   + gyroX(2) + gyroY(2) + gyroZ(2) + leftTrigger(1) + rightTrigger(1) = 18

    private fun encodeControllerState(buf: ByteArray, offset: Int, state: ControllerState) {
        var pos = offset
        putUShortLE(buf, pos, state.buttons); pos += 2
        putShortLE(buf, pos, state.leftThumbX); pos += 2
        putShortLE(buf, pos, state.leftThumbY); pos += 2
        putShortLE(buf, pos, state.rightThumbX); pos += 2
        putShortLE(buf, pos, state.rightThumbY); pos += 2
        putShortLE(buf, pos, state.gyroX); pos += 2
        putShortLE(buf, pos, state.gyroY); pos += 2
        putShortLE(buf, pos, state.gyroZ); pos += 2
        buf[pos] = state.leftTrigger.toByte(); pos += 1
        buf[pos] = state.rightTrigger.toByte()
    }

    // -- Little-endian helpers --

    private fun putLongLE(buf: ByteArray, offset: Int, value: Long) {
        for (i in 0..7) {
            buf[offset + i] = (value shr (i * 8)).toByte()
        }
    }

    private fun putUIntLE(buf: ByteArray, offset: Int, value: UInt) {
        val v = value.toInt()
        buf[offset] = v.toByte()
        buf[offset + 1] = (v shr 8).toByte()
        buf[offset + 2] = (v shr 16).toByte()
        buf[offset + 3] = (v shr 24).toByte()
    }

    private fun putUShortLE(buf: ByteArray, offset: Int, value: UShort) {
        val v = value.toInt()
        buf[offset] = v.toByte()
        buf[offset + 1] = (v shr 8).toByte()
    }

    private fun putShortLE(buf: ByteArray, offset: Int, value: Short) {
        val v = value.toInt()
        buf[offset] = v.toByte()
        buf[offset + 1] = (v shr 8).toByte()
    }
}
