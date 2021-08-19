/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.InfuraRequest
import dev.icerock.moko.web3.entity.InfuraResponse
import dev.icerock.moko.web3.entity.Transaction
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.serializer.BigIntSerializer
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.coroutineContext

class Web3(
    private val httpClient: HttpClient,
    private val json: Json,
    private val infuraUrl: String
) : Web3RPC {

    suspend fun <T> batch(block: BatchScope.() -> List<Deferred<T>>): List<T> {
        val requestInfoChannel = Channel<BatchRPC.RequestInfo>()
        return withContext(coroutineContext + BatchRPC(requestInfoChannel)) {
            val batchScope: BatchScope = object : BatchScope,
                Web3RPC by this@Web3,
                CoroutineScope by this {}

            val deferredList: List<Deferred<T>> = batchScope.block()
            val requestInfos = mutableListOf<BatchRPC.RequestInfo>()

            for (i in deferredList.indices) {
                val info = requestInfoChannel.receive()
                requestInfos.add(info)
            }

            val batchArray: JsonArray = requestInfos.map { info ->
                info.requestJson
            }.let { JsonArray(it) }

            val response: HttpResponse = httpClient.post {
                url(infuraUrl)
                body = batchArray.outgoingContent
            }

            val resultJson: JsonArray = json.decodeFromString(
                deserializer = JsonArray.serializer(),
                string = response.readText()
            )

            resultJson.forEach { resultElement ->
                val resultObject = resultElement as JsonObject
                val id: Int = resultObject.getValue("id").jsonPrimitive.int
                val rpcCall: BatchRPC.RequestInfo = requestInfos.first { it.id == id }
                rpcCall.resultChannel.send(resultElement)
            }

            deferredList.awaitAll()
        }
    }

    private fun BatchRPC.getBatchMaxCallId(): Int {
        return nextRequestId++
    }

    override suspend fun getTransaction(transactionHash: TransactionHash): Transaction {
        return executeRPC(
            method = "eth_getTransactionByHash",
            params = listOf(transactionHash.value),
            paramsSerializer = String.serializer(),
            resultSerializer = Transaction.serializer()
        )
    }

    override suspend fun getTransactionReceipt(transactionHash: TransactionHash): TransactionReceipt {
        return executeRPC(
            method = "eth_getTransactionReceipt",
            params = listOf(transactionHash.value),
            paramsSerializer = String.serializer(),
            resultSerializer = TransactionReceipt.serializer()
        )
    }

    override suspend fun getEthBalance(
        walletAddress: WalletAddress,
        blockState: BlockState
    ): BigInt {
        return executeRPC(
            method = "eth_getBalance",
            params = listOf(walletAddress.value, blockState.toString()),
            paramsSerializer = String.serializer(),
            resultSerializer = BigIntSerializer
        )
    }

    override suspend fun getEthTransactionCount(
        walletAddress: WalletAddress,
        blockState: BlockState
    ): BigInt {
        return executeRPC(
            method = "eth_getTransactionCount",
            params = listOf(walletAddress.value, blockState.toString()),
            paramsSerializer = String.serializer(),
            resultSerializer = BigIntSerializer
        )
    }

    override suspend fun <T> call(
        transactionCall: JsonElement,
        responseDataSerializer: KSerializer<T>,
        blockState: BlockState
    ): T {
        return executeRPC(
            method = "eth_call",
            params = listOf(
                transactionCall,
                JsonPrimitive(blockState.toString())
            ),
            paramsSerializer = JsonElement.serializer(),
            resultSerializer = responseDataSerializer
        )
    }

    override suspend fun send(
        signedTransaction: String
    ): TransactionHash {
        return executeRPC(
            method = "eth_sendRawTransaction",
            params = listOf(signedTransaction),
            paramsSerializer = String.serializer(),
            resultSerializer = String.serializer()
        ).let { TransactionHash(it) }
    }

    private suspend fun <T, R> executeRPC(
        method: String,
        params: List<T>,
        paramsSerializer: KSerializer<T>,
        resultSerializer: KSerializer<R>
    ): R {
        val batchCall: BatchRPC? = coroutineContext[BatchRPC]

        val requestSerializer = InfuraRequest.serializer(paramsSerializer)
        val callId: Int = batchCall?.getBatchMaxCallId() ?: 0
        val request: InfuraRequest<T> = InfuraRequest(
            method = method,
            params = params,
            id = callId
        )
        val requestJson: JsonElement = json.encodeToJsonElement(requestSerializer, request)

        val response: JsonElement = if (batchCall != null) {
            val resultChannel = Channel<JsonElement>()
            val requestInfo = BatchRPC.RequestInfo(
                id = callId,
                requestJson = requestJson,
                resultChannel = resultChannel
            )
            batchCall.requestInfoChannel.send(requestInfo)

            resultChannel.receive()
        } else {
            val response: HttpResponse = httpClient.post {
                url(infuraUrl)
                body = requestJson.outgoingContent
            }
            json.decodeFromString(JsonElement.serializer(), response.readText())
        }

        return processResponse(
            request = request,
            serializer = InfuraResponse.serializer(resultSerializer),
            content = response
        )
    }

    private fun <T> processResponse(
        request: InfuraRequest<*>,
        serializer: KSerializer<InfuraResponse<T>>,
        content: JsonElement
    ): T {
        val infuraResponse = json.decodeFromJsonElement(serializer, content)

        when {
            infuraResponse.result != null -> return infuraResponse.result
            infuraResponse.error != null -> throw Web3RpcException(
                code = infuraResponse.error.code,
                message = infuraResponse.error.message,
                request = request
            )
            else -> throw UnknownWeb3RpcException(
                request = request,
                response = content.toString()
            )
        }
    }

    private val JsonElement.outgoingContent: OutgoingContent
        get() {
            return TextContent(
                text = this.toString(),
                contentType = ContentType.Application.Json
            )
        }

    interface BatchScope : Web3RPC, CoroutineScope
}
