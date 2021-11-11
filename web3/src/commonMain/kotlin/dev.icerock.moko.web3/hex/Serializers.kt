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

object HexStringSerializer : ParametrizedHexStringSerializer<HexString>(HexString)
object Hex8StringSerializer : ParametrizedHexStringSerializer<Hex8String>(Hex8String)
object Hex16StringSerializer : ParametrizedHexStringSerializer<Hex16String>(Hex16String)
object Hex32StringSerializer : ParametrizedHexStringSerializer<Hex32String>(Hex32String)
object Hex64StringSerializer : ParametrizedHexStringSerializer<Hex64String>(Hex64String)
object Hex128StringSerializer : ParametrizedHexStringSerializer<Hex128String>(Hex128String)
object Hex256StringSerializer : ParametrizedHexStringSerializer<Hex256String>(Hex256String)
