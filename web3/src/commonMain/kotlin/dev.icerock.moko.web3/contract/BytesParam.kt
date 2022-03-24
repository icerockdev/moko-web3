/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.contract.ABIEncoder.PART_SIZE

object BytesParam : DynamicEncoder<ByteArray> {
    override fun encode(item: ByteArray): ByteArray {
        val sizeData = UInt256Param.encode(item.size.bi)

        return item
            .asIterable()
            .chunked(PART_SIZE)
            .map {
                it.toByteArray() + ByteArray(PART_SIZE - it.size)
            }.fold(initial = sizeData) { acc, bytes ->
                acc + bytes
            }
    }

    override fun decode(source: ByteArray): ByteArray {
        val size = UInt256Param.decode(source.take(PART_SIZE).toByteArray()).toInt()
        val extraZeros = PART_SIZE - (size % PART_SIZE)

        return source
            .asList()
            .drop(PART_SIZE)
            .dropLast(extraZeros)
            .toByteArray()
    }
}