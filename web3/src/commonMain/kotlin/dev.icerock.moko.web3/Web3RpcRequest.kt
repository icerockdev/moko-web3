/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlinx.serialization.KSerializer

data class Web3RpcRequest<T, R>(
    val method: String,
    val params: List<T>,
    val paramsSerializer: KSerializer<T>,
    val resultSerializer: KSerializer<R>
)
