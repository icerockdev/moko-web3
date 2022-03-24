/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.entity.RpcRequest

data class Web3RpcException(
    val code: Int,
    override val message: String,
    val request: RpcRequest<*>
) : Exception(message)
