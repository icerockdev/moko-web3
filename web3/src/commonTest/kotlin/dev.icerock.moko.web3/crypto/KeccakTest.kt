/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.crypto

import dev.icerock.moko.web3.hex.internal.hexStringToByteArray
import dev.icerock.moko.web3.hex.internal.toHex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeccakTest {
    @Test
    fun `test keccak`() {
        assertTrue(
            "".digestKeccak(KeccakParameter.KECCAK_256)
                .contentEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470".hexStringToByteArray())
        )

        assertTrue(
            "The quick brown fox jumps over the lazy dog".digestKeccak(KeccakParameter.KECCAK_256)
                .contentEquals("4d741b6f1eb29cb2a9b9911c82f56fa8d73b04959d3d9d222895df6c0b28aa15".hexStringToByteArray())
        )

        assertTrue(
            "The quick brown fox jumps over the lazy dog.".digestKeccak(KeccakParameter.KECCAK_256)
                .contentEquals("578951e24efd62a3d63a86f7cd19aaa53c898fe287d2552133220370240b572d".hexStringToByteArray())
        )

        assertTrue(
            "The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog.The quick brown fox jumps over the lazy dog."
                .digestKeccak(KeccakParameter.KECCAK_256)
                .contentEquals("e35949d2ca446ea2fd99f49bed23c60e0b9849f5384661bc574a5c55fcaeb4bd".hexStringToByteArray())
        )
    }

    @Test
    fun `test signature`() {
        assertEquals(
            actual = "liquidationIncentiveMantissa()".digestKeccak(KeccakParameter.KECCAK_256).toHex().lowercase(),
            expected = "4ada90af6b44d25a0b11928b22a71ad7cf4bea72796dfddcef4cc8e97183ccd5"
        )

        assertEquals(
            actual = "liquidationIncentiveMantissa()".digestKeccak(KeccakParameter.KECCAK_256)
                .copyOfRange(0, 10).toHex().lowercase(),
            expected = "4ada90af6b44d25a0b11"
        )
    }
}
