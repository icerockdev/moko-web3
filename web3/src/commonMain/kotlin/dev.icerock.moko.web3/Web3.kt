/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.annotation.DelicateWeb3Api
import dev.icerock.moko.web3.entity.RpcRequest
import dev.icerock.moko.web3.entity.RpcResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

/**
 * Default http implementation for web3 requests.
 * @delicate Don't use the default constructor since it is for test purposes only
 */
class Web3 @DelicateWeb3Api constructor(
    private val httpClient: HttpClient,
    private val json: Json,
    private val endpointUrl: String
) : Web3Executor {

    override suspend fun <T, R> executeBatch(vararg requests: Web3RpcRequest<T, R>): List<R> {
        // Used later for logging if exception
        val rawRequests = requests
            .mapIndexed { index, web3Request ->
                RpcRequest(
                    method = web3Request.method,
                    id = index,
                    params = web3Request.params
                )
            }

        val encodedToStringBody = rawRequests
            .mapIndexed { index, request ->
                val encodedParams = request.params.map { param ->
                    json.encodeToJsonElement(
                        serializer = requests[index].paramsSerializer,
                        value = param
                    )
                }
                json.encodeToJsonElement(
                    serializer = RpcRequest.serializer(JsonElement.serializer()),
                    // cannot use copy since generics mismatch
                    value = RpcRequest(
                        method = request.method,
                        id = request.id,
                        jsonrpc = request.jsonrpc,
                        params = encodedParams
                    )
                )
            }.let { list -> json.encodeToString(list) }

        val responses = httpClient
            .post<String> {
                url(endpointUrl)
                body = encodedToStringBody
            }.let { raw ->
                json.decodeFromString<List<JsonObject>>(raw)
            }

        // Here we are restoring the order
        return requests.mapIndexed { index, request ->
            val response = responses.first { response ->
                val id = response.getValue(key = "id").jsonPrimitive.int
                return@first id == index
            }

            return@mapIndexed processResponse(
                request = rawRequests[index],
                deserializer = request.resultSerializer,
                content = response.toString()
            )
        }
    }

    private fun <T> processResponse(
        request: RpcRequest<*>,
        deserializer: DeserializationStrategy<T>,
        content: String
    ): T {
        val response = json.decodeFromString(RpcResponse.serializer(JsonElement.serializer()), content)
        val typedResponse = RpcResponse(
            jsonrpc = response.jsonrpc,
            id = response.id,
            result = response.result?.let { json.decodeFromJsonElement(deserializer, it) },
            error = response.error
        )

        when {
            typedResponse.result != null -> return typedResponse.result
            typedResponse.error != null -> throw Web3RpcException(
                code = typedResponse.error.code,
                message = typedResponse.error.message,
                request = request
            )
            else -> throw UnknownWeb3RpcException(
                request = request,
                response = content
            )
        }
    }
}
