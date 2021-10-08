/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.hex.Hex8String

class WalletAddress(value: String) : EthereumAddress by EthereumAddress(value) {
    override fun toString() = withoutPrefix 
    override fun hashCode(): Int = withoutPrefix.hashCode()
    override fun equals(other: Any?): Boolean = other is WalletAddress && withoutPrefix == other.withoutPrefix
}
