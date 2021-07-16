/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

interface Encoder<T : Any> {
    fun encode(data: T): ByteArray
    fun decode(byteArray: ByteArray): T
}
