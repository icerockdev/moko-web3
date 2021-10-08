/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.crypto.createChecksummedAddress
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString

interface EthereumAddress : Hex32String {
    companion object : HexString.Factory<EthereumAddress> {
        override fun createInstance(value: String): EthereumAddress = EthereumAddress(value)
    }
}

@Suppress("ClassName")
private class _EthereumAddress(value: String) : EthereumAddress, Hex32String by Hex32String(value)

fun EthereumAddress(value: String): EthereumAddress = _EthereumAddress(value)

fun EthereumAddress.toChecksummedAddress(): EthereumAddress =
    createChecksummedAddress(sourceAddress = this, factoryTypeclass = EthereumAddress)

val EthereumAddress.isChecksummed: Boolean get() = withoutPrefix == toChecksummedAddress().withoutPrefix

val EthereumAddress.isValid: Boolean get() =
    withoutPrefix.uppercase() == withoutPrefix
            || withoutPrefix.lowercase() == withoutPrefix
            || isChecksummed
