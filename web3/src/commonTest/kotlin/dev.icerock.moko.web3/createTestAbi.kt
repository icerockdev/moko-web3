/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlinx.serialization.json.Json
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
      },
      {
        "components": [
          {
            "name": "address",
            "type": "address"
          },
          {
            "name": "list",
            "type": "address[]"
          }
        ],
        "name": "tuple",
        "type": "tuple"
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
  },
  {
    "constant": false,
    "inputs": [
      {
        "name": "stringType",
        "type": "string"
      },
      {
        "name": "not dynamic type",
        "type": "uint256"
      },
      {
        "name": "bytesType",
        "type": "bytes"
      }
    ],
    "name": "testDynamicEncoder",
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
fun createTestAbi(json: Json) = json.parseToJsonElement(testAbiRaw).jsonArray
