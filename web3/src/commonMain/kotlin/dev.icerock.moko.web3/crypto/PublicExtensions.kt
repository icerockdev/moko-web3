/*
 * Copyright (c) 2017 ligi. Use of this source code is governed by the MIT License.
 * Original: https://github.com/komputing/KHash/blob/master/keccak/src/main/kotlin/org/komputing/khash/keccak/extensions/PublicExtensions.kt
 */

package dev.icerock.moko.web3.crypto

import io.ktor.utils.io.core.toByteArray

/**
 * Computes the proper Keccak digest of [this] byte array based on the given [parameter]
 */
fun ByteArray.digestKeccak(parameter: KeccakParameter): ByteArray {
    return Keccak.digest(this, parameter)
}

/**
 * Computes the proper Keccak digest of [this] string based on the given [parameter]
 */
fun String.digestKeccak(parameter: KeccakParameter): ByteArray {
    return Keccak.digest(toByteArray(), parameter)
}
