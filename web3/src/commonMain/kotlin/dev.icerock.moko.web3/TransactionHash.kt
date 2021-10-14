/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.hex.Hex32String
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TransactionHash.Serializer::class)
class TransactionHash(private val value: String) : Hex32String by Hex32String(value) {
    override fun toString() = withoutPrefix
    override fun hashCode(): Int = withoutPrefix.hashCode()
    override fun equals(other: Any?): Boolean = other is TransactionHash && other.withoutPrefix == withoutPrefix

    @kotlinx.serialization.Serializer(forClass = TransactionHash::class)
    object Serializer {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            serialName = "dev.icerock.mokoWeb3.TransactionHash",
            kind = PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): TransactionHash = TransactionHash(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: TransactionHash) = encoder.encodeString(value.value)
    }
}
