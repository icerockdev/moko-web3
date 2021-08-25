/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlin.jvm.JvmInline

@JvmInline
value class ContractAddress(override val value: String) : EthereumAddress
