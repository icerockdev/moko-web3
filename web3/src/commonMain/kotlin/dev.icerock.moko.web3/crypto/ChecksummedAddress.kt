/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.crypto

import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

/**
 * Explanation: https://coincodex.com/article/2078/ethereum-address-checksum-explained/
 * Algorithm: https://ethereum.stackexchange.com/questions/1374/how-can-i-check-if-an-ethereum-address-is-valid,
 *  https://github.com/ethers-io/ethers.js/blob/ce8f1e4015c0f27bf178238770b1325136e3351a/packages/address/src.ts/index.ts#L12
 */
fun <T : EthereumAddress> createChecksummedAddress(
    sourceAddress: T,
    factoryTypeclass: HexString.Factory<T>
): T = with(sourceAddress) {
    val hashed = withoutPrefix
        .lowercase()
        .keccakHash
        .asHexInts

    val result = withoutPrefix
        .mapIndexed { i, char -> char.takeIf { hashed[i] < 8 } ?: char.uppercase() }
        .joinToString(separator = "")

    return factoryTypeclass.createInstance(result)
}

private val String.keccakHash get(): ByteArray = this
    .map { it.code.toByte() }
    .toByteArray()
    .let { Keccak.digest(it, KeccakParameter.KECCAK_256) }

// Split the every byte to 2 numbers as
// If it would be hex representation (byte 255 is ff, 15 and 15)
private val ByteArray.asHexInts get(): List<Int> =
    flatMap { byte ->
        with(byte.toUByte().toInt()) {
            listOf(
                // (FA) shr 4 = (1111 1010) shr 4 -> 1111 = f = 15
                shr(bitCount = 4),
                // (FA) and (0x0f) = (1111 1010) and (0000 ffff) -> 1010 = a = 10
                and(other = 0x0f)
            )
        }
    }
