/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("ClassName")

package dev.icerock.moko.web3

import dev.icerock.moko.web3.hex.ParametrizedHexStringSerializer

object Erc20TokenAddressSerializer : ParametrizedHexStringSerializer<Erc20TokenAddress>(Erc20TokenAddress)

class Erc20TokenAddress(value: String) : ContractAddress(value) {
    companion object : SizedFactory<Erc20TokenAddress> {
        override val size = 20
        override fun createInstance(value: String) = Erc20TokenAddress(value)
    }
}
