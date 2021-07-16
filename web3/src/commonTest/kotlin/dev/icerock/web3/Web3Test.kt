/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.web3

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.web3.contract.AddressParam
import dev.icerock.web3.contract.SmartContract
import dev.icerock.web3.contract.UInt256Param
import dev.icerock.web3.contract.createErc20TokenAbi
import dev.icerock.web3.crypto.toHex
import dev.icerock.web3.entity.TransactionReceipt
import io.ktor.client.HttpClient
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class Web3Test {
    private val infuraUrl = "https://rinkeby.infura.io/v3/5a3d2c30cf72450c9e13b0570a737b62"
    private val httpClient: HttpClient = HttpClient() {
        install(Logging){
            logger = object :Logger{
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.ALL
        }
    }

    @Test
    fun `read transaction`() {
        val web3 = Web3(
            httpClient = httpClient,
            infuraUrl = infuraUrl,
            json = Json
        )
        val txHash = TransactionHash("0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352")

        val result = runTest {
            web3.getTransaction(txHash)
        }

        assertEquals(actual = result.txHash, expected = txHash)
        assertEquals(actual = result.gasPrice, expected = BigInt(8000000000))
        assertEquals(actual = result.gasLimit, expected = BigInt(322020))
        assertEquals(actual = result.transactionIndex, expected = BigInt(1))
        assertEquals(actual = result.nonce, expected = BigInt(74))
        assertEquals(
            actual = result.blockHash,
            expected = BlockHash("0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b")
        )
        assertEquals(
            actual = result.blockNumber,
            expected = BigInt(6352955)
        )
        assertEquals(
            actual = result.senderAddress,
            expected = WalletAddress("0xde7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c")
        )
        assertEquals(
            actual = result.receiverAddress,
            expected = WalletAddress("0xd6801a1dffcd0a410336ef88def4320d6df1883e")
        )
        assertEquals(
            actual = result.input,
            expected = "0x852a12e300000000000000000000000000000000000000000000000000470de4df820000"
        )
    }

    @Test
    fun `read transaction receipt`() {
        val web3 = Web3(
            httpClient = httpClient,
            infuraUrl = infuraUrl,
            json = Json
        )
        val txHash = TransactionHash("0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352")

        val result = runTest {
            web3.getTransactionReceipt(txHash)
        }

        assertEquals(actual = result.txHash, expected = txHash)
        assertEquals(actual = result.contractAddress, expected = null)
        assertEquals(actual = result.cumulativeGasUsed, expected = BigInt(241293))
        assertEquals(actual = result.gasUsed, expected = BigInt(214821))
        assertEquals(actual = result.transactionIndex, expected = BigInt(1))
        assertEquals(actual = result.status, expected = TransactionReceipt.Status.SUCCESS)
        assertEquals(actual = result.blockNumber, expected = BigInt(6352955))
        assertEquals(
            actual = result.blockHash,
            expected = BlockHash("0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b")
        )
        assertEquals(
            actual = result.senderAddress,
            expected = WalletAddress("0xde7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c")
        )
        assertEquals(
            actual = result.receiverAddress,
            expected = WalletAddress("0xd6801a1dffcd0a410336ef88def4320d6df1883e")
        )

        result.logs.forEach { event ->
            assertEquals(
                actual = event.blockHash,
                expected = BlockHash("0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b")
            )
            assertEquals(actual = event.txHash, expected = txHash)
            assertEquals(actual = event.blockNumber, expected = BigInt(6352955))
            assertEquals(actual = result.transactionIndex, expected = BigInt(1))
        }

        assertEquals(
            actual = result.logs[0].topics,
            expected = listOf("0x875352fb3fadeb8c0be7cbbe8ff761b308fa7033470cd0287f02f3436fd76cb9")
        )

        assertEquals(
            actual = result.logs[1].topics,
            expected = listOf(
                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                "0x000000000000000000000000de7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c",
                "0x000000000000000000000000d6801a1dffcd0a410336ef88def4320d6df1883e"
            )
        )

        assertEquals(
            actual = result.logs[2].topics,
            expected = listOf("0xe5b754fb1abb7f01b499791d0b820ae3b6af3424ac1c59768edb53f4ec31a929")
        )
    }

    @Test
    fun `smart contract params`() {
        val json = Json
        val web3 = Web3(
            httpClient = httpClient,
            infuraUrl = infuraUrl,
            json = json
        )
        val smartContract = SmartContract(
            web3 = web3,
            contractAddress = ContractAddress("0x6b175474e89094c44da98b954eedeac495271d0f"),
            abiJson = createErc20TokenAbi(json)
        )

        val addr = "9a0A2498Ec7f105ef65586592a5B6d4Da3590D74".bi(16)

        val result = runTest {
            smartContract.encodeTransaction(
                method = "transfer",
                params = listOf(
                    addr,
                    BigInt(0x10001000)
                )
            )
        }

        assertEquals(
            expected = "0xa9059cbb0000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d740000000000000000000000000000000000000000000000000000000010001000",
            actual = result.data
        )
    }

    @Test
    fun `address encoding`() {
        val param = AddressParam()
        val addr = "9a0A2498Ec7f105ef65586592a5B6d4Da3590D74".bi(16)
        val result = param.encode(addr)
        val hex = result.toHex()

        assertEquals(
            expected = "0000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d74",
            actual = hex.toLowerCase()
        )

        val address = param.decode(result)
        assertEquals(
            expected = addr,
            actual = address
        )
    }

    @Test
    fun `unit256 encoding`() {
        val param = UInt256Param()
        val input = "1234567891011adfdfdeadfea123d34cd342dcd234234ffeedd342432ddff555".bi(16)
        val result = param.encode(input)
        val hex = result.toHex()

        assertEquals(
            expected = "1234567891011adfdfdeadfea123d34cd342dcd234234ffeedd342432ddff555",
            actual = hex.toLowerCase()
        )

        val output = param.decode(result)
        assertEquals(
            expected = input,
            actual = output
        )
    }

    @Test
    fun `smart contract encoding`() {
        val json = Json
        val web3 = Web3(
            httpClient = httpClient,
            infuraUrl = infuraUrl,
            json = json
        )
        val smartContract = SmartContract(
            web3 = web3,
            contractAddress = ContractAddress("0x5592ec0cfb4dbc12d3ab100b257153436a1f0fea"),
            abiJson = createErc20TokenAbi(json)
        )

        val to = "9a0A2498Ec7f105ef65586592a5B6d4Da3590D74".bi(16)
        val sum = "16345785d8a0000".bi(16)

        val result = runTest {
            smartContract.encodeTransaction(
                method = "transfer",
                from = WalletAddress("0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C"),
                params = listOf(
                    to,
                    sum
                )
            )
        }

        assertEquals(
            expected = "0xa9059cbb0000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d74000000000000000000000000000000000000000000000000016345785d8a0000",
            actual = result.data
        )
    }

    @Test
    fun `read balance`() {
        val json = Json
        val web3 = Web3(
            httpClient = httpClient,
            infuraUrl = infuraUrl,
            json = json
        )

        val result = runTest {
            web3.getEthBalance(WalletAddress("0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C"))
        }

        println("balance $result")
    }
}
