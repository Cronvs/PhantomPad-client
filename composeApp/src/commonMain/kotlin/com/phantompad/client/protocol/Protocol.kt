package com.phantompad.client.protocol

/**
 * PhantomPad binary protocol definitions.
 * Matches the C++ server at https://github.com/prevter/phantompad-server
 *
 * All multi-byte fields are little-endian.
 * All packets are packed with no padding (matching #pragma pack(push, 1)).
 */

const val PROTOCOL_VERSION: UShort = 1u
const val SERVER_PORT: Int = 35700

// -- Packet type bytes --

object PacketType {
    const val DISCOVERY_REQUEST: Byte = 0x01
    const val DISCOVERY_RESPONSE: Byte = 0x02
    const val CONNECT_REQUEST: Byte = 0x03
    const val CONNECT_RESPONSE: Byte = 0x04
    const val DISCONNECT: Byte = 0x05
    const val PING: Byte = 0x06
    const val PONG: Byte = 0x07
    const val INPUT: Byte = 0x08
    const val HAPTIC_FEEDBACK: Byte = 0x09
}

// -- Server icon types --

enum class ServerIcon(val value: Byte) {
    Generic(0x00),
    Desktop(0x01),
    Laptop(0x02),
    HTPC(0x03),
    SteamDeck(0x04);

    companion object {
        fun fromByte(b: Byte): ServerIcon =
            entries.firstOrNull { it.value == b } ?: Generic
    }
}

// -- Connection status codes --

enum class ConnectStatus(val value: Byte) {
    Success(0x00),
    ServerFull(0x01),
    VersionMismatch(0x02),
    ControllerInitFailure(0x03),
    ViGEmNotFound(0x04);

    companion object {
        fun fromByte(b: Byte): ConnectStatus =
            entries.firstOrNull { it.value == b } ?: ControllerInitFailure
    }

    fun toMessage(): String = when (this) {
        Success -> "Connected"
        ServerFull -> "Server is full (max 8 players)"
        VersionMismatch -> "Protocol version mismatch"
        ControllerInitFailure -> "Server failed to create controller"
        ViGEmNotFound -> "ViGEmBus driver not found on server"
    }
}

// -- Controller button flags (bitmask, uint16) --

object ControllerButtons {
    const val A: UShort = 0x0001u
    const val B: UShort = 0x0002u
    const val X: UShort = 0x0004u
    const val Y: UShort = 0x0008u
    const val LEFT_BUMPER: UShort = 0x0010u
    const val RIGHT_BUMPER: UShort = 0x0020u
    const val BACK: UShort = 0x0040u
    const val START: UShort = 0x0080u
    const val GUIDE: UShort = 0x0100u
    const val LEFT_THUMB: UShort = 0x0200u
    const val RIGHT_THUMB: UShort = 0x0400u
    const val DPAD_UP: UShort = 0x0800u
    const val DPAD_DOWN: UShort = 0x1000u
    const val DPAD_LEFT: UShort = 0x2000u
    const val DPAD_RIGHT: UShort = 0x4000u
}

// -- Controller state (18 bytes packed) --

data class ControllerState(
    var buttons: UShort = 0u,
    var leftThumbX: Short = 0,
    var leftThumbY: Short = 0,
    var rightThumbX: Short = 0,
    var rightThumbY: Short = 0,
    var gyroX: Short = 0,
    var gyroY: Short = 0,
    var gyroZ: Short = 0,
    var leftTrigger: UByte = 0u,
    var rightTrigger: UByte = 0u,
) {
    companion object {
        const val SIZE_BYTES = 18
    }
}

// -- Parsed server response types --

data class DiscoveryResponse(
    val icon: ServerIcon,
    val timestamp: Long,
    val name: String,
    val address: String,
    val port: Int,
    val latencyMs: Long = 0,
)

data class ConnectResponse(
    val status: ConnectStatus,
    val slotId: Int,
    val serverName: String,
)

data class HapticFeedback(
    val strongMagnitude: UShort,
    val weakMagnitude: UShort,
)
