/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

object StringParam : DynamicEncoder<String> {
    override fun encode(item: String): ByteArray = item
        .toByteArray(Charsets.ISO_8859_1)
        .let(BytesParam::encode)

    override fun decode(source: ByteArray): String = BytesParam
        .decode(source)
        .decodeToString()
}
