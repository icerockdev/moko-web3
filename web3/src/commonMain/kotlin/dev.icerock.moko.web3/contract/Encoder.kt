/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

sealed interface Encoder<T> {
    fun encode(item: T): ByteArray
    fun decode(source: ByteArray): T
}

/* These types created to split decoding head and dynamic data */

interface StaticEncoder<T> : Encoder<T>
interface DynamicEncoder<T> : Encoder<T>
