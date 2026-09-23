package com.phantompad.client.network

import kotlinx.cinterop.*
import platform.darwin.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual class UdpSocket actual constructor() {
    private var fd: Int = -1

    actual fun open() {
        fd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
        if (fd < 0) throw Exception("Failed to create UDP socket")

        // Allow address reuse
        memScoped {
            val optVal = alloc<IntVar>()
            optVal.value = 1
            setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, optVal.ptr, sizeOf<IntVar>().toUInt())
        }
    }

    actual fun enableBroadcast() {
        if (fd < 0) return
        memScoped {
            val optVal = alloc<IntVar>()
            optVal.value = 1
            setsockopt(fd, SOL_SOCKET, SO_BROADCAST, optVal.ptr, sizeOf<IntVar>().toUInt())
        }
    }

    actual fun sendTo(address: String, port: Int, data: ByteArray) {
        if (fd < 0) return
        memScoped {
            val addr = alloc<sockaddr_in>()
            addr.sin_family = AF_INET.toUByte()
            addr.sin_port = htons(port.toUShort())
            inet_pton(AF_INET, address, addr.sin_addr.ptr)

            data.usePinned { pinned ->
                platform.posix.sendto(
                    fd,
                    pinned.addressOf(0),
                    data.size.toULong(),
                    0,
                    addr.ptr.reinterpret(),
                    sizeOf<sockaddr_in>().toUInt()
                )
            }
        }
    }

    actual fun receive(timeoutMs: Int): UdpDatagram? {
        if (fd < 0) return null

        memScoped {
            // Use select() for timeout
            if (timeoutMs > 0) {
                val readSet = alloc<fd_set>()
                posix_FD_ZERO(readSet.ptr)
                posix_FD_SET(fd, readSet.ptr)

                val timeout = alloc<timeval>()
                timeout.tv_sec = (timeoutMs / 1000).toLong()
                timeout.tv_usec = ((timeoutMs % 1000) * 1000)

                val result = select(fd + 1, readSet.ptr, null, null, timeout.ptr)
                if (result <= 0) return null
            }

            val buf = ByteArray(65536)
            val senderAddr = alloc<sockaddr_in>()
            val addrLen = alloc<socklen_tVar>()
            addrLen.value = sizeOf<sockaddr_in>().toUInt()

            val bytesRead = buf.usePinned { pinned ->
                recvfrom(
                    fd,
                    pinned.addressOf(0),
                    buf.size.toULong(),
                    0,
                    senderAddr.ptr.reinterpret(),
                    addrLen.ptr
                )
            }

            if (bytesRead <= 0) return null

            // Convert sender address to string
            val addrBuf = ByteArray(INET_ADDRSTRLEN)
            val addrStr = addrBuf.usePinned { pinned ->
                inet_ntop(AF_INET, senderAddr.sin_addr.ptr, pinned.addressOf(0), addrBuf.size.toUInt())
            }

            return UdpDatagram(
                data = buf,
                size = bytesRead.toInt(),
                address = addrStr?.toKString() ?: "",
                port = ntohs(senderAddr.sin_port).toInt(),
            )
        }
    }

    actual fun close() {
        if (fd >= 0) {
            platform.posix.close(fd)
            fd = -1
        }
    }

    actual val isOpen: Boolean get() = fd >= 0
}

// Helper functions for fd_set operations on Darwin
@OptIn(ExperimentalForeignApi::class)
private fun posix_FD_ZERO(set: CPointer<fd_set>) {
    memset(set, 0, sizeOf<fd_set>().toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun posix_FD_SET(fd: Int, set: CPointer<fd_set>) {
    // fd_set on Darwin is an array of __int32_t with 32 bits each
    val index = fd / 32
    val bit = fd % 32
    val arrayPtr = set.reinterpret<IntVar>()
    val current = arrayPtr[index]
    arrayPtr[index] = current or (1 shl bit)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentTimeMillis(): Long {
    memScoped {
        val tv = alloc<timeval>()
        gettimeofday(tv.ptr, null)
        return tv.tv_sec * 1000L + tv.tv_usec / 1000L
    }
}
