/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import dev.icerock.moko.web3.contract.ABIEncoder
import dev.icerock.moko.web3.contract.AddressParam
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.contract.UInt256Param
import dev.icerock.moko.web3.contract.createErc20TokenAbi
import dev.icerock.moko.web3.entity.RpcResponse
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.internal.toHex
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.executeBatch
import dev.icerock.moko.web3.requests.getEstimateGas
import dev.icerock.moko.web3.requests.getGasPrice
import dev.icerock.moko.web3.requests.getNativeBalance
import dev.icerock.moko.web3.requests.getNativeTransactionCount
import dev.icerock.moko.web3.requests.getTransaction
import dev.icerock.moko.web3.requests.getTransactionReceipt
import dev.icerock.moko.web3.requests.polling.newBlocksShortPolling
import dev.icerock.moko.web3.requests.polling.newLogsShortPolling
import dev.icerock.moko.web3.requests.waitForTransactionReceipt
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.content.TextContent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Web3Test {
    private val infuraUrl = "https://rinkeby.infura.io/v3/5a3d2c30cf72450c9e13b0570a737b62"

    private fun createTestWeb3(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): Web3 = Web3(
        httpClient = createMockClient(handler),
        endpointUrl = infuraUrl,
        json = Json
    )

    @Test
    fun `read transaction`() {
        val web3 = createTestWeb3 { request ->
            val body = request.body
            assertTrue(body is TextContent)
            assertEquals(
                expected = """[{"jsonrpc":"2.0","id":0,"method":"eth_getTransactionByHash","params":["0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352"]}]""",
                actual = body.text
            )
            respond(content = """[{"jsonrpc":"2.0","id":0,"result":{"blockHash":"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b","blockNumber":"0x60f03b","from":"0xde7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c","gas":"0x4e9e4","gasPrice":"0x1dcd65000","hash":"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352","input":"0x852a12e300000000000000000000000000000000000000000000000000470de4df820000","nonce":"0x4a","r":"0x5aeca7a54ae3bb0f67a29aece00d71bc75c0d06b89950ea600a0b3b6bbfe5e8c","s":"0x68fefa5333e94443dca19e30562bf297b8a687abf15c3fe2671de6233299fff0","to":"0xd6801a1dffcd0a410336ef88def4320d6df1883e","transactionIndex":"0x1","type":"0x0","v":"0x2c","value":"0x0"}}]""")
        }
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
        val web3 = createTestWeb3 { request ->
            val body = request.body
            assertTrue(body is TextContent)
            assertEquals(
                expected = """[{"jsonrpc":"2.0","id":0,"method":"eth_getTransactionReceipt","params":["0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352"]}]""",
                actual = body.text
            )
            respond(
                content = """[{"jsonrpc":"2.0","id":0,"result":{"blockHash":"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b","blockNumber":"0x60f03b","contractAddress":null,"cumulativeGasUsed":"0x3ae8d","effectiveGasPrice":"0x1dcd65000","from":"0xde7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c","gasUsed":"0x34725","logs":[{"address":"0xd6801a1dffcd0a410336ef88def4320d6df1883e","blockHash":"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b","blockNumber":"0x60f03b","data":"0x00000000000000000000000000000000000000000000000000078a9f2e72421c000000000000000000000000000000000000000000000000103938f95c5bb8de00000000000000000000000000000000000000000000007e6f395eb639577b12","logIndex":"0x0","removed":false,"topics":["0x875352fb3fadeb8c0be7cbbe8ff761b308fa7033470cd0287f02f3436fd76cb9"],"transactionHash":"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352","transactionIndex":"0x1"},{"address":"0xd6801a1dffcd0a410336ef88def4320d6df1883e","blockHash":"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b","blockNumber":"0x60f03b","data":"0x00000000000000000000000000000000000000000000000000000000052f2dd9","logIndex":"0x1","removed":false,"topics":["0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef","0x000000000000000000000000de7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c","0x000000000000000000000000d6801a1dffcd0a410336ef88def4320d6df1883e"],"transactionHash":"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352","transactionIndex":"0x1"},{"address":"0xd6801a1dffcd0a410336ef88def4320d6df1883e","blockHash":"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b","blockNumber":"0x60f03b","data":"0x000000000000000000000000de7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c00000000000000000000000000000000000000000000000000470de4df82000000000000000000000000000000000000000000000000000000000000052f2dd9","logIndex":"0x2","removed":false,"topics":["0xe5b754fb1abb7f01b499791d0b820ae3b6af3424ac1c59768edb53f4ec31a929"],"transactionHash":"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352","transactionIndex":"0x1"}],"logsBloom":"0x00000080000000000000000000000000000000000000000000000000000000000000040000100000800000000000000000000000000000000000000000000000000000000000000000000808000000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000010000000000000000080000000000000000000000000000000000000800000000000000001000000000000000000000000000000000000000000000000000000002000000000002000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000020200000000004","status":"0x1","to":"0xd6801a1dffcd0a410336ef88def4320d6df1883e","transactionHash":"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352","transactionIndex":"0x1","type":"0x0"}}]"""
            )
        }
        val txHash = TransactionHash("0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352")

        val result = runTest {
            web3.getTransactionReceipt(txHash) ?: error("This transaction should exist")
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
            assertEquals(actual = event.transactionHash, expected = txHash)
            assertEquals(actual = event.blockNumber, expected = BigInt(6352955))
            assertEquals(actual = result.transactionIndex, expected = BigInt(1))
        }

        assertEquals(
            actual = result.logs[0].topics,
            expected = listOf(Hex32String("0x875352fb3fadeb8c0be7cbbe8ff761b308fa7033470cd0287f02f3436fd76cb9"))
        )

        assertEquals(
            actual = result.logs[1].topics,
            expected = listOf(
                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                "0x000000000000000000000000de7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c",
                "0x000000000000000000000000d6801a1dffcd0a410336ef88def4320d6df1883e"
            ).map(::Hex32String)
        )

        assertEquals(
            actual = result.logs[2].topics,
            expected = listOf(Hex32String("0xe5b754fb1abb7f01b499791d0b820ae3b6af3424ac1c59768edb53f4ec31a929"))
        )
    }

    @Test
    fun `smart contract params`() {
        val json = Json
        val executor = createTestWeb3 { respondBadRequest() }
        val smartContract = SmartContract(
            executor = executor,
            contractAddress = ContractAddress("0x6b175474e89094c44da98b954eedeac495271d0f"),
            abiJson = createErc20TokenAbi(json)
        )

        val addr = "9a0A2498Ec7f105ef65586592a5B6d4Da3590D74".bi(16)

        val result = runTest {
            ABIEncoder.encodeCallDataForMethod(
                abi = createErc20TokenAbi(json),
                method = "transfer",
                params = listOf(
                    addr,
                    BigInt(0x10001000)
                )
            )
        }

        assertEquals(
            expected = "0xa9059cbb0000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d740000000000000000000000000000000000000000000000000000000010001000".let(
                ::HexString
            ),
            actual = result
        )
    }

    @Test
    fun `address encoder`() {
        val param = AddressParam
        val addr = "9a0A2498Ec7f105ef65586592a5B6d4Da3590D74".bi(16)
        val result = param.encode(addr)
        val hex = result.toHex()

        assertEquals(
            expected = "0000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d74",
            actual = hex.lowercase()
        )

        val address = param.decode(result)
        assertEquals(
            expected = addr,
            actual = address
        )
    }

    @Test
    fun `unit256 encoder`() {
        val param = UInt256Param
        val input = "1234567891011adfdfdeadfea123d34cd342dcd234234ffeedd342432ddff555".bi(16)
        val result = param.encode(input)
        val hex = result.toHex()

        assertEquals(
            expected = "1234567891011adfdfdeadfea123d34cd342dcd234234ffeedd342432ddff555",
            actual = hex.lowercase()
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
        val executor = createTestWeb3 { respondBadRequest() }
        val smartContract = SmartContract(
            executor = executor,
            contractAddress = ContractAddress(value = "0x0000000000000000000000000000000000000000"),
            abiJson = createTestAbi(json)
        )

        val address = "9a0A2498Ec7f105ef65586592a5B6d4Da3590D74".bi(16)
        val bigInt = "16345785d8a0000".bi(16)
        val list = listOf(address, bigInt)

        val result = runTest {
            ABIEncoder.encodeCallDataForMethod(
                abi = createTestAbi(json),
                method = "test",
                params = listOf(
                    address,
                    bigInt,
                    list,
                    listOf(
                        address,
                        list
                    )
                )
            )
        }
        assertEquals(
            expected = "0x170159cd0000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d74000000000000000000000000000000000000000000000000016345785d8a0000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000e000000000000000000000000000000000000000000000000000000000000000020000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d74000000000000000000000000000000000000000000000000016345785d8a00000000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d74000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000020000000000000000000000009a0a2498ec7f105ef65586592a5b6d4da3590d74000000000000000000000000000000000000000000000000016345785d8a0000".let(
                ::HexString
            ),
            actual = result
        )
    }

    @Test
    fun `read balance`() {
        val web3 = createTestWeb3 { request ->
            val body = request.body
            assertTrue(body is TextContent)
            assertEquals(
                expected = """[{"jsonrpc":"2.0","id":0,"method":"eth_getBalance","params":["0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C","latest"]}]""",
                actual = body.text
            )
            respond(content = """[{"jsonrpc":"2.0","id":0,"result":"0xd94ec060b14773c7"}]""")
        }

        val result = runTest {
            web3.getNativeBalance(WalletAddress("0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C"))
        }

        assertEquals(
            expected = "15658664475937436615".bi,
            actual = result
        )
    }

    @Test
    fun `get nonce`() {
        val web3 = createTestWeb3 { request ->
            val body = request.body
            assertTrue(body is TextContent)
            assertEquals(
                expected = """[{"jsonrpc":"2.0","id":0,"method":"eth_getTransactionCount","params":["0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C","pending"]}]""",
                actual = body.text
            )
            respond(content = """[{"jsonrpc":"2.0","id":0,"result":"0x1275"}]""")
        }

        val result = runTest {
            web3.getNativeTransactionCount(WalletAddress("0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C"))
        }

        assertEquals(
            expected = 4725.bi,
            actual = result
        )
    }

    @Test
    fun `batch test`() {
        val web3 = createTestWeb3 { request ->
            val body = request.body
            assertTrue(body is TextContent)
            assertEquals(
                expected = """[{"jsonrpc":"2.0","id":0,"method":"eth_getTransactionCount","params":["0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C","pending"]},{"jsonrpc":"2.0","id":1,"method":"eth_getBalance","params":["0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C","latest"]}]""",
                actual = body.text
            )
            respond(content = """[{"jsonrpc":"2.0","id":0,"result":"0x1275"},{"jsonrpc":"2.0","id":1,"result":"0xd94ec060b14773c7"}]""")
        }

        val wallet = WalletAddress("0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C")

        val (nonce, balance) = runTest {
            web3.executeBatch(
                Web3Requests.getNativeTransactionCount(wallet),
                Web3Requests.getNativeBalance(wallet),
            )
        }

        assertEquals(
            expected = 4725.bi,
            actual = nonce
        )
        assertEquals(
            expected = "15658664475937436615".bi,
            actual = balance
        )
    }

    @Test
    fun `gas price test`() {
        val web3 = createTestWeb3 { request ->
            val body = request.body
            assertTrue(body is TextContent)
            assertEquals(
                expected = """[{"jsonrpc":"2.0","id":0,"method":"eth_gasPrice","params":[]}]""",
                actual = body.text
            )
            respond(content = """[{"jsonrpc":"2.0","id":0,"result":"0x3b9aca08"}]""")
        }
        runTest {
            assertEquals(
                expected = 1000000008.bi,
                actual = web3.getGasPrice()
            )
        }
    }

    //    @Test
    fun `short polling test`() {
        runTest {
            println(
                Json.decodeFromString(
                    RpcResponse.serializer(JsonElement.serializer()),
                    """{"jsonrpc":"","id":0,"result":null}"""
                ).result as JsonNull
            )
            val web3 = Web3("https://rinkeby.infura.io/v3/5a3d2c30cf72450c9e13b0570a737b62")
            web3.waitForTransactionReceipt(
                TransactionHash("0x6f7914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352"),
                timeOutMillis = 5_000
            )
        }
    }

    //    @Test
    fun `new blocks short polling test`() {
        runBlocking {
            val web3 = Web3("https://rinkeby.infura.io/v3/5a3d2c30cf72450c9e13b0570a737b62")
            web3.newBlocksShortPolling(pollingInterval = 5_000)
                .collect { println("Block ${it.hash} mined!") }
        }
    }

    //    @Test
    fun `new logs short polling test`() {
        runBlocking {
            val web3 = Web3("https://rinkeby.infura.io/v3/5a3d2c30cf72450c9e13b0570a737b62")
            web3.newLogsShortPolling(pollingInterval = 5_000)
                .collect { println("Log $it caught!") }
        }
    }

    //    @Test
    fun legacyTransactionForming() {
        runBlocking {
            val web3 = Web3("https://api.avax-test.network/ext/bc/C/rpc")
            val price = web3.getGasPrice()
            println("GAS Price: $price")
        }
    }

    //    @Test
    fun legacyExtendedTransactionForming() {
        runBlocking {
            val web3 = Web3("https://bsc.getblock.io/testnet/?api_key=94c96d69-74f0-40e7-8202-eac4b49e6bfc")
            val price = web3.getGasPrice()
            val callData =
                HexString("0x38ed17390000000000000000000000000000000000000000000000000de0b6b3a764000000000000000000000000000000000000000000000000000000002e57839a043800000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000140e21fcfb1e602a1626198d3dbbb58087b59b4e0000000000000000000000000000000000000000000000000000000061e8f26700000000000000000000000000000000000000000000000000000000000000030000000000000000000000009a01bf917477dd9f5d715d188618fc8b7350cd22000000000000000000000000ae13d989dac2f0debff460ac112a837c89baa7cd00000000000000000000000041b5984f45afb2560a0ed72bb69a98e8b32b3cca")
            val to = ContractAddress("0xc43d2c472cf882e0b190063d66ee8ce78bf54da1")
            val from = EthereumAddress("0x140e21fcfb1e602a1626198d3dbbb58087b59b4e")
            val value = 2_000_000_000_000_000.bi
            println("GAS Price: $price")
            println(
                "GAS Limit: ${
                    web3.getEstimateGas(
                        to = to,
                        from = from,
                        gasPrice = price,
                        callData = callData,
                        value = value
                    ).toBigNum().times(1.1.bn)
                }"
            )
        }
    }
}

