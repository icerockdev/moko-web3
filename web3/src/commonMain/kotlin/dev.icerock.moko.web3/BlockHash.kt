/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.ParametrizedHexStringSerializer
import kotlinx.serialization.Serializable

object BlockHashSerializer : ParametrizedHexStringSerializer<BlockHash>(BlockHash)

@Serializable(with = BlockHashSerializer::class)
class BlockHash(value: String) : Hex32String(value) {
    companion object : Factory<BlockHash> {
        override fun createInstance(value: String): BlockHash = BlockHash(value)
    }
}
