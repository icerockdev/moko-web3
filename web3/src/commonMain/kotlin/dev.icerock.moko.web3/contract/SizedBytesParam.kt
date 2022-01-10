/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

class SizedBytesParam(private val size: Int) : StaticEncoder<ByteArray> {
    override fun encode(item: ByteArray): ByteArray {
        require(item.size == size) { "ByteArray size is not equal to the required one" }
        return item
    }

    override fun decode(source: ByteArray): ByteArray {
        require(source.size == size) { "ByteArray size is not equal to the required one" }
        return source
    }
}
