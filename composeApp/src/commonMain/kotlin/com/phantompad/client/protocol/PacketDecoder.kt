package com.phantompad.client.protocol

/**
 * Decodes server responses from raw UDP byte arrays.
 * All multi-byte values are little-endian.
 */
object PacketDecoder {

    /**
     * Returns the packet type byte, or null if the data is empty.
     */
    fun packetType(data: ByteArray, size: Int): Byte? {
        if (size < 1) return null
        return data[0]
    }

    /**
     * DiscoveryResponse: type(1) + icon(1) + timestamp(8) + nameLength(1) + name(N)
     * Minimum size: 11 bytes
     */
    fun discoveryResponse(data: ByteArray, size: Int): DiscoveryResponse? {
        if (size < 11) return null
        if (data[0] != PacketType.DISCOVERY_RESPONSE) return null

        val icon = ServerIcon.fromByte(data[1])
        val timestamp = getLongLE(data, 2)
        val nameLen = data[10].toInt() and 0xFF
        if (size < 11 + nameLen) return null

        val name = data.decodeToString(11, 11 + nameLen)
        return DiscoveryResponse(
            icon = icon,
            timestamp = timestamp,
            name = name,
            address = "", // filled in by caller
            port = SERVER_PORT,
        )
    }

    /**
     * ConnectResponse: type(1) + status(1) + slotId(1) + nameLength(1) + name(N)
     * Minimum size: 4 bytes
     */
    fun connectResponse(data: ByteArray, size: Int): ConnectResponse? {
        if (size < 4) return null
        if (data[0] != PacketType.CONNECT_RESPONSE) return null

        val status = ConnectStatus.fromByte(data[1])
        val slotId = data[2].toInt() and 0xFF
        val nameLen = data[3].toInt() and 0xFF
        val serverName = if (size >= 4 + nameLen && nameLen > 0) {
            data.decodeToString(4, 4 + nameLen)
        } else {
            "Unknown"
        }

        return ConnectResponse(
            status = status,
            slotId = slotId,
            serverName = serverName,
        )
    }

    /**
     * Pong: type(1) + timestamp(8) = 9 bytes
     */
    fun pong(data: ByteArray, size: Int): Long? {
        if (size < 9) return null
        if (data[0] != PacketType.PONG) return null
        return getLongLE(data, 1)
    }

    /**
     * HapticFeedback: type(1) + strongMagnitude(2) + weakMagnitude(2) = 5 bytes
     */
    fun hapticFeedback(data: ByteArray, size: Int): HapticFeedback? {
        if (size < 5) return null
        if (data[0] != PacketType.HAPTIC_FEEDBACK) return null
        return HapticFeedback(
            strongMagnitude = getUShortLE(data, 1),
            weakMagnitude = getUShortLE(data, 3),
        )
    }

    /**
     * Disconnect: type(1) = 1 byte
     */
    fun isDisconnect(data: ByteArray, size: Int): Boolean {
        return size >= 1 && data[0] == PacketType.DISCONNECT
    }

    // -- Little-endian helpers --

    private fun getLongLE(data: ByteArray, offset: Int): Long {
        var result = 0L
        for (i in 7 downTo 0) {
            result = (result shl 8) or (data[offset + i].toLong() and 0xFF)
        }
        return result
    }

    private fun getUShortLE(data: ByteArray, offset: Int): UShort {
        val lo = data[offset].toInt() and 0xFF
        val hi = data[offset + 1].toInt() and 0xFF
        return ((hi shl 8) or lo).toUShort()
    }
}
