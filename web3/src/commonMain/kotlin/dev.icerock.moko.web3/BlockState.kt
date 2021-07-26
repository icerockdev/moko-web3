/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlin.jvm.JvmInline


sealed interface BlockState {
    override fun toString(): String

    object Latest : BlockState {
        override fun toString() = "latest"
    }
    object Earliest : BlockState {
        override fun toString() = "earliest"
    }
    object Pending : BlockState {
        override fun toString() = "pending"
    }

    @JvmInline
    value class Quantity(private val blockNumber: String) : BlockState {
        override fun toString() = blockNumber
    }
}
