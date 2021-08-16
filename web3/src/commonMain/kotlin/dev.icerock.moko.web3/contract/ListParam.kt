/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.bi

class ListParam<T>(private val subtypeEncoder: StaticEncoder<T>) : DynamicEncoder<List<T>> {
    override fun encode(item: List<T>): ByteArray {
        val sizeEncoded = UInt256Param.encode(item.size.bi)
        return item
            .map(subtypeEncoder::encode)
            .fold(sizeEncoded) { acc, part -> acc + part }
    }
    override fun decode(source: ByteArray): List<T> {
        val chunkedSource = source.toList().chunked(size = SmartContract.PART_SIZE)
        // We drop first element since the first element is always list size
        return chunkedSource.drop(n = 1)
            .map { subtypeEncoder.decode(it.toByteArray()) }
    }
}
