/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.content.TextContent

// When new test added, it should be tested with this real client,
// And then mocked request should be added to client below,
// since networks are unstable (especially test networks)

fun createRealClient(): HttpClient = HttpClient {
    install(Logging){
        logger = object : Logger {
            override fun log(message: String) {
                println(message)
            }
        }
        level = LogLevel.ALL
    }
}

fun createMockClient(): HttpClient = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            val responseText = when((request.body as TextContent).text) {
                "[{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"eth_getBalance\",\"params\":[\"0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C\",\"latest\"]}]" ->
                    "[{\"jsonrpc\":\"2.0\",\"id\":0,\"result\":\"0xd94ec060b14773c7\"}]"
                "[{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"eth_getTransactionCount\",\"params\":[\"0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C\",\"pending\"]}]" ->
                    "[{\"jsonrpc\":\"2.0\",\"id\":0,\"result\":\"0x1275\"}]"
                "[{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"eth_getTransactionCount\",\"params\":[\"0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C\",\"pending\"]},{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"eth_getBalance\",\"params\":[\"0xdE7eC4E4895D7d148906a0DFaAF7f21ac5C5B80C\",\"latest\"]}]" ->
                    "[{\"jsonrpc\":\"2.0\",\"id\":0,\"result\":\"0x1275\"},{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"0xd94ec060b14773c7\"}]"
                "[{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"eth_getTransactionByHash\",\"params\":[\"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352\"]}]" ->
                    "[{\"jsonrpc\":\"2.0\",\"id\":0,\"result\":{\"blockHash\":\"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b\",\"blockNumber\":\"0x60f03b\",\"from\":\"0xde7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c\",\"gas\":\"0x4e9e4\",\"gasPrice\":\"0x1dcd65000\",\"hash\":\"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352\",\"input\":\"0x852a12e300000000000000000000000000000000000000000000000000470de4df820000\",\"nonce\":\"0x4a\",\"r\":\"0x5aeca7a54ae3bb0f67a29aece00d71bc75c0d06b89950ea600a0b3b6bbfe5e8c\",\"s\":\"0x68fefa5333e94443dca19e30562bf297b8a687abf15c3fe2671de6233299fff0\",\"to\":\"0xd6801a1dffcd0a410336ef88def4320d6df1883e\",\"transactionIndex\":\"0x1\",\"type\":\"0x0\",\"v\":\"0x2c\",\"value\":\"0x0\"}}]"
                "[{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"eth_getTransactionReceipt\",\"params\":[\"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352\"]}]" ->
                    "[{\"jsonrpc\":\"2.0\",\"id\":0,\"result\":{\"blockHash\":\"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b\",\"blockNumber\":\"0x60f03b\",\"contractAddress\":null,\"cumulativeGasUsed\":\"0x3ae8d\",\"effectiveGasPrice\":\"0x1dcd65000\",\"from\":\"0xde7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c\",\"gasUsed\":\"0x34725\",\"logs\":[{\"address\":\"0xd6801a1dffcd0a410336ef88def4320d6df1883e\",\"blockHash\":\"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b\",\"blockNumber\":\"0x60f03b\",\"data\":\"0x00000000000000000000000000000000000000000000000000078a9f2e72421c000000000000000000000000000000000000000000000000103938f95c5bb8de00000000000000000000000000000000000000000000007e6f395eb639577b12\",\"logIndex\":\"0x0\",\"removed\":false,\"topics\":[\"0x875352fb3fadeb8c0be7cbbe8ff761b308fa7033470cd0287f02f3436fd76cb9\"],\"transactionHash\":\"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352\",\"transactionIndex\":\"0x1\"},{\"address\":\"0xd6801a1dffcd0a410336ef88def4320d6df1883e\",\"blockHash\":\"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b\",\"blockNumber\":\"0x60f03b\",\"data\":\"0x00000000000000000000000000000000000000000000000000000000052f2dd9\",\"logIndex\":\"0x1\",\"removed\":false,\"topics\":[\"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef\",\"0x000000000000000000000000de7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c\",\"0x000000000000000000000000d6801a1dffcd0a410336ef88def4320d6df1883e\"],\"transactionHash\":\"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352\",\"transactionIndex\":\"0x1\"},{\"address\":\"0xd6801a1dffcd0a410336ef88def4320d6df1883e\",\"blockHash\":\"0x3d62f862d25cf7015a485868b07825484fd7a51f77a9e7863fe45ec8a61db01b\",\"blockNumber\":\"0x60f03b\",\"data\":\"0x000000000000000000000000de7ec4e4895d7d148906a0dfaaf7f21ac5c5b80c00000000000000000000000000000000000000000000000000470de4df82000000000000000000000000000000000000000000000000000000000000052f2dd9\",\"logIndex\":\"0x2\",\"removed\":false,\"topics\":[\"0xe5b754fb1abb7f01b499791d0b820ae3b6af3424ac1c59768edb53f4ec31a929\"],\"transactionHash\":\"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352\",\"transactionIndex\":\"0x1\"}],\"logsBloom\":\"0x00000080000000000000000000000000000000000000000000000000000000000000040000100000800000000000000000000000000000000000000000000000000000000000000000000808000000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000010000000000000000080000000000000000000000000000000000000800000000000000001000000000000000000000000000000000000000000000000000000002000000000002000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000020200000000004\",\"status\":\"0x1\",\"to\":\"0xd6801a1dffcd0a410336ef88def4320d6df1883e\",\"transactionHash\":\"0x627914c8d005ab0dc7f44719dc658af72e534e083867a2a316d4b25555515352\",\"transactionIndex\":\"0x1\",\"type\":\"0x0\"}}]"
                "[{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"eth_gasPrice\",\"params\":[]}]" ->
                    "[{\"jsonrpc\":\"2.0\",\"id\":0,\"result\":\"0x3b9aca08\"}]"
                else -> error("Unknown request!")
            }
            return@addHandler respond(responseText)
        }
    }
}
