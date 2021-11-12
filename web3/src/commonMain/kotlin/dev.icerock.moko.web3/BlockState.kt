/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.hex.HexString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline


object BlockStateSerializer : KSerializer<BlockState> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "BlockStateStringSerializer",
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): BlockState = when (val string = decoder.decodeString()) {
        "latest" -> BlockState.Latest
        "earliest" -> BlockState.Earliest
        "pending" -> BlockState.Pending
        else -> BlockState.Quantity(string.bi)
    }

    override fun serialize(encoder: Encoder, value: BlockState) = encoder.encodeString(value.toString())

}

@Serializable(with = BlockStateSerializer::class)
sealed interface BlockState {
    override fun toString(): String

    object Latest : BlockState {
        override fun toString() = "latest"
    }
    object Earliest : BlockState {
        override fun toString() = "earliest"
    }
    object Pending : BlockState {
        override fun toString() = "pending"
    }

    @JvmInline
    value class Quantity(private val blockNumber: BigInt) : BlockState {
        override fun toString() = HexString(blockNumber.toString(radix = 16), strict = false).prefixed
    }
}
