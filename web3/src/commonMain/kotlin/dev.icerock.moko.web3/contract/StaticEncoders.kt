/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

enum class StaticEncoders(
    val typeAnnotation: String,
    val encoder: StaticEncoder<*>
) {
    UInt256(typeAnnotation = "uint256", encoder = UInt256Param),
    Address(typeAnnotation = "address", encoder = AddressParam);

    companion object {
        fun forType(typeAnnotation: String) = values()
            .firstOrNull { it.typeAnnotation == typeAnnotation }
            ?: error("There is no such encoder for type $typeAnnotation")
    }
}
