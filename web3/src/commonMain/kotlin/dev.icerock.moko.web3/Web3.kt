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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class Web3(
    private val httpClient: HttpClient,
    private val json: Json,
    private val infuraUrl: String
) {
    suspend fun getTransaction(transactionHash: TransactionHash): Transaction {
        val requestSerializer = InfuraRequest.serializer(String.serializer())
        val request: InfuraRequest<String> = InfuraRequest(
            method = "eth_getTransactionByHash",
            params = listOf(transactionHash.value)
        )

        val response: HttpResponse = httpClient.post {
            url(infuraUrl)
            body = json.encodeToJsonElement(requestSerializer, request).outgoingContent
        }

        return processResponse(
            request = request,
            serializer = InfuraResponse.serializer(Transaction.serializer()),
            content = response.readText()
        )
    }

    suspend fun getTransactionReceipt(transactionHash: TransactionHash): TransactionReceipt {
        val requestSerializer = InfuraRequest.serializer(String.serializer())
        val request: InfuraRequest<String> = InfuraRequest(
            method = "eth_getTransactionReceipt",
            params = listOf(transactionHash.value)
        )

        val response: HttpResponse = httpClient.post {
            url(infuraUrl)
            body = json.encodeToJsonElement(requestSerializer, request).outgoingContent
        }

        return processResponse(
            request = request,
            serializer = InfuraResponse.serializer(TransactionReceipt.serializer()),
            content = response.readText()
        )
    }

    suspend fun getEthBalance(walletAddress: WalletAddress): BigInt {
        val requestSerializer = InfuraRequest.serializer(String.serializer())
        val request: InfuraRequest<String> = InfuraRequest(
            method = "eth_getBalance",
            params = listOf(walletAddress.value, "latest")
        )

        val response: HttpResponse = httpClient.post {
            url(infuraUrl)
            body = json.encodeToJsonElement(requestSerializer, request).outgoingContent
        }

        return processResponse(
            request = request,
            serializer = InfuraResponse.serializer(BigIntSerializer),
            content = response.readText()
        )
    }

    suspend fun getEthTransactionCount(walletAddress: WalletAddress): BigInt {
        val requestSerializer = InfuraRequest.serializer(String.serializer())
        val request: InfuraRequest<String> = InfuraRequest(
            method = "eth_getTransactionCount",
            params = listOf(walletAddress.value, "latest")
        )

        val response: HttpResponse = httpClient.post {
            url(infuraUrl)
            body = json.encodeToJsonElement(requestSerializer, request).outgoingContent
        }

        return processResponse(
            request = request,
            serializer = InfuraResponse.serializer(BigIntSerializer),
            content = response.readText()
        )
    }

    suspend fun <T> call(
        transactionCall: JsonElement,
        responseDataSerializer: KSerializer<T>
    ): T {
        val requestSerializer = InfuraRequest.serializer(JsonElement.serializer())
        val request: InfuraRequest<JsonElement> = InfuraRequest(
            method = "eth_call",
            params = listOf(
                transactionCall,
                JsonPrimitive("latest")
            )
        )

        val response: HttpResponse = httpClient.post {
            url(infuraUrl)
            body = json.encodeToJsonElement(requestSerializer, request).outgoingContent
        }

        return processResponse(
            request = request,
            serializer = InfuraResponse.serializer(responseDataSerializer),
            content = response.readText()
        )
    }

    suspend fun send(
        signedTransaction: String
    ): TransactionHash {
        val requestSerializer = InfuraRequest.serializer(String.serializer())
        val request: InfuraRequest<String> = InfuraRequest(
            method = "eth_sendRawTransaction",
            params = listOf(signedTransaction)
        )

        val response: HttpResponse = httpClient.post {
            url(infuraUrl)
            body = json.encodeToJsonElement(requestSerializer, request).outgoingContent
        }

        return processResponse(
            request = request,
            serializer = InfuraResponse.serializer(String.serializer()),
            content = response.readText()
        ).let { TransactionHash(it) }
    }

    private fun <T> processResponse(
        request: InfuraRequest<*>,
        serializer: KSerializer<InfuraResponse<T>>,
        content: String
    ): T {
        val infuraResponse = json.decodeFromString(serializer, content)

        when {
            infuraResponse.result != null -> return infuraResponse.result
            infuraResponse.error != null -> throw Web3RpcException(
                code = infuraResponse.error.code,
                message = infuraResponse.error.message,
                request = request
            )
            else -> throw UnknownWeb3RpcException(
                request = request,
                response = content
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
}
