/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.ParametrizedHexStringSerializer
import kotlinx.serialization.Serializable


object WalletAddressSerializer : ParametrizedHexStringSerializer<WalletAddress>(WalletAddress)

@Serializable(with = WalletAddressSerializer::class)
class WalletAddress(value: String) : EthereumAddress by EthereumAddress(value) {
    override fun toString() = withoutPrefix 
    override fun hashCode(): Int = withoutPrefix.hashCode()
    override fun equals(other: Any?): Boolean = other is WalletAddress && withoutPrefix == other.withoutPrefix

    companion object : HexString.Factory<WalletAddress> {
        override fun createInstance(value: String): WalletAddress = WalletAddress(value)
    }
}
