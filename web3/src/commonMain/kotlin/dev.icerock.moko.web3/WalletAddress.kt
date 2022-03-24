/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.hex.ParametrizedHexStringSerializer
import kotlinx.serialization.Serializable

object WalletAddressSerializer : ParametrizedHexStringSerializer<WalletAddress>(WalletAddress)

@Serializable(with = WalletAddressSerializer::class)
class WalletAddress(value: String) : EthereumAddress(value) {
    companion object : SizedFactory<WalletAddress> {
        override val size: Int = 20
        override fun createInstance(value: String): WalletAddress = WalletAddress(value)
    }
}
