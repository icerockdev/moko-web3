/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex.internal

private val HEX_CHARS = "0123456789abcdef".toCharArray()

internal fun ByteArray.toHex(): String {
    val result = StringBuilder()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}

/**
 * @param unsafe if true then it automatically adds leading zero if the length is even
 */
internal fun String.hexStringToByteArray(unsafe: Boolean = false): ByteArray {
    val string = if(unsafe) hexStringAddLeadingZeroIfNeed() else this
    require(string.length % 2 == 0) { "Hex string length should be odd" }

    return string.removePrefix(prefix = "0x")
        .map { it }  // Converting to a list of chars to use destructors in map
        .chunked(size = 2)
        .map { (firstPart, secondPart) -> convertOctetToByte(firstPart, secondPart) }
        .toByteArray()
}

private fun convertOctetToByte(firstPart: Char, secondPart: Char): Byte {
    val firstHex = firstPart.digitToInt(radix = 16)
    val secondHex = secondPart.digitToInt(radix = 16)
    val octet = (firstHex shl 4) or secondHex
    return octet.toByte()
}
