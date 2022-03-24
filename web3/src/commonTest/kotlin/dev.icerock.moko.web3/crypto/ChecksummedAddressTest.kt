/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.crypto

import dev.icerock.moko.web3.ContractAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChecksummedAddressTest {
    @Test
    fun `test to checksummed address`() {
        val contractAddress = ContractAddress(value = "0xC9472182861faD74b6810cf58C792501F8837473").also(::println)
        assertEquals(
            expected = contractAddress,
            actual = contractAddress.toChecksummedAddress().also(::println)
        )
    }

    @Test
    fun `test is valid`() {
        val contractAddress = ContractAddress(value = "0xC9472182861faD74b6810cf58C792501F8837473")
        assertTrue(contractAddress.isValid)
        val contractAddressNotChecksummedUppercase = ContractAddress(value = contractAddress.withoutPrefix.uppercase())
        assertTrue(contractAddressNotChecksummedUppercase.isValid)
        val contractAddressNotChecksummedLowercase = ContractAddress(value = contractAddress.withoutPrefix.lowercase())
        assertTrue(contractAddressNotChecksummedLowercase.isValid)
    }
}
