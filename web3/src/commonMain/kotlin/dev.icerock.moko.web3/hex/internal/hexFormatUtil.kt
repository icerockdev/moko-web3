/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex.internal

internal fun String.hexStringFillToSizedHex(size: Int): String {
    val currentSize = length / 2

    require(currentSize <= size) { "This hex string already has more bytes than the target one" }

    val isStrictlyValid = length % 2 == 0

    return buildString {
        repeat(times = size - currentSize) {
            append("00")
        }
        if (!isStrictlyValid)
            append("0")
        append(this@hexStringFillToSizedHex)
    }
}

internal fun String.hexStringAddLeadingZeroIfNeed() = takeIf { length % 2 == 0 } ?: "0$this"

internal fun ByteArray.hexBytesFillToSizedHex(size: Int): ByteArray {
    require(size <= this.size) { "This hex byte array already has more bytes than the target one" }
    val padLeft = ByteArray(size = size - this.size)
    return padLeft + this
}
