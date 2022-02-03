/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.requests

import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3RpcRequest
import dev.icerock.moko.web3.hex.HexString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
private class CallDataObject(
    val to: ContractAddress,
    val data: HexString,
)

class CallRpcRequest<T>(
    val contractAddress: ContractAddress,
    val callData: HexString,
    // deserialize from calldata to normal type
    val dataDeserializer: DeserializationStrategy<T>,
    val blockState: BlockState = BlockState.Latest
) : Web3RpcRequest<JsonElement, T>(
    method = "eth_call",
    params = listOf(
        Json.encodeToJsonElement(CallDataObject(contractAddress, callData)),
        JsonPrimitive(blockState.toString())
    ),
    paramsSerializer = JsonElement.serializer(),
    resultSerializer = dataDeserializer
)
