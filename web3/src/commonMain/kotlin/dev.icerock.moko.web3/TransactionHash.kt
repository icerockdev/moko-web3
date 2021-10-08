/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.hex.Hex32String

class TransactionHash(value: String) : Hex32String by Hex32String(value)
