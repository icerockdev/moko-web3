/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

interface Erc20TokenAddress : ContractAddress

private class _Erc20TokenAddress(value: String) : Erc20TokenAddress, ContractAddress by ContractAddress(value)

fun Erc20TokenAddress(value: String): Erc20TokenAddress = _Erc20TokenAddress(value)
