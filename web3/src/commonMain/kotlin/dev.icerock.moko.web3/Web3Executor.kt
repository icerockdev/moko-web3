/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

/**
 * Common interface for web3 methods
 */
interface Web3Executor {
    /**
     * @throws Web3RpcException in case of some exception returned by remote
     *
     * It is allowed to throw something else if it is not related to Web3 (internet exceptions, etc.)
     */
    suspend fun <R> executeBatch(requests: List<Web3RpcRequest<*, out R>>): List<R>
}
