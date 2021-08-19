/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */


package dev.icerock.moko.web3

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray

// language=json
private const val testAbiRaw = """
    [
        {
            "constant": false,
            "inputs": [
                {
                    "name": "address",
                    "type": "address"
                },
                {
                    "name": "int",
                    "type": "uint256"
                },
                {
                    "name": "list",
                    "type": "uint256[]"
                }
            ],
            "name": "test",
            "outputs": [
                {
                    "name": "",
                    "type": "bool"
                }
            ],
            "payable": false,
            "stateMutability": "nonpayable",
            "type": "function"
        }
    ]
"""

internal fun createTestAbi(json: Json): JsonArray = json.parseToJsonElement(testAbiRaw).jsonArray
