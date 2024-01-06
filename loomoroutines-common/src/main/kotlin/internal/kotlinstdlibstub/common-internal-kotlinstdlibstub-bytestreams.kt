@file:JvmName("ByteStreamsKt")

package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

fun InputStream.readBytes(): ByteArray {
    val buffer = ByteArrayOutputStream(maxOf(DEFAULT_BUFFER_SIZE, this.available()))
    copyTo(buffer)
    return buffer.toByteArray()
}

fun InputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}