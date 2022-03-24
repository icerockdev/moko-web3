/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.crypto

import dev.icerock.moko.web3.hex.internal.toHex
import io.ktor.utils.io.core.toByteArray

object KeccakId {
    fun get(bytes: ByteArray): ByteArray = bytes.digestKeccak(KeccakParameter.KECCAK_256).sliceArray(0..3)
    fun getString(string: String): String = get(string.toByteArray()).toHex()
}
