/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.crypto.createChecksummedAddress
import dev.icerock.moko.web3.hex.Hex20String
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.Hex8String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.ParametrizedHexStringSerializer
import kotlinx.serialization.Serializable

object EthereumAddressSerializer : ParametrizedHexStringSerializer<EthereumAddress>(EthereumAddress)

@Serializable(with = EthereumAddressSerializer::class)
open class EthereumAddress(value: String) : Hex20String(value) {
    companion object : SizedFactory<EthereumAddress> {
        override val size: Int = 20
        override fun createInstance(value: String): EthereumAddress = EthereumAddress(value)

        val AddressZero = EthereumAddress.createInstance(0.bi)
    }

    open fun toChecksummedAddress(): EthereumAddress =
        createChecksummedAddress(sourceAddress = this, factoryTypeclass = EthereumAddress)
    val isChecksummed: Boolean get() = withoutPrefix == toChecksummedAddress().withoutPrefix
    val isValid: Boolean get() =
        withoutPrefix.uppercase() == withoutPrefix
                || withoutPrefix.lowercase() == withoutPrefix
                || isChecksummed
}
