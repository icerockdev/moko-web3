/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.contract.ABIDecoder
import dev.icerock.moko.web3.contract.ABIEncoder
import dev.icerock.moko.web3.hex.HexString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestEncoder {

    // Checked, everything works fine
    // to check it, please visit https://abi.hashex.org and place there your abi
    @Test
    fun `test string encoder`() {
        val testString = "test"
        val testNotDynamicParam = 0.bi
        val testBytes = byteArrayOf(0, 11, 22, 33, 44, 55, 66, 77)

        val abiJson = createTestAbi(Json)
        val callData = ABIEncoder.encodeCallDataForMethod(
            abi = abiJson,
            method = "testDynamicEncoder",
            params = listOf(testString, testNotDynamicParam, testBytes)
        )

        val expected = "0xa2d155530000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000000474657374000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000b16212c37424d000000000000000000000000000000000000000000000000".let(::HexString)
        assertEquals(expected, callData)

        val decoded = ABIDecoder.decodeCallData(
            HexString("0000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000000474657374000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000b16212c37424d000000000000000000000000000000000000000000000000").byteArray,
            "string", "uint256", "bytes"
        )

        assertEquals(testString, decoded.first())
        assertEquals(testNotDynamicParam, decoded[1])
        assertContentEquals(testBytes, decoded[2] as ByteArray)
    }
}
