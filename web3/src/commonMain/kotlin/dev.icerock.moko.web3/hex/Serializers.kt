/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class ParametrizedHexStringSerializer<T : HexString>(private val typeclass: HexString.Factory<T>) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "HexStringSerializer",
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): T = typeclass.createInstance(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.prefixed)
}
