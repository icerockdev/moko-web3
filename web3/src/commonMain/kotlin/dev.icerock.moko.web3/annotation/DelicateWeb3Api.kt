/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.annotation

@RequiresOptIn(
    message = """
        This api is delicate, maybe it contains another delicate method call(s) or have some behaviour that you should use at our own risk.
        Probably you can find more info in the comment.
    """,
    level = RequiresOptIn.Level.WARNING
)
annotation class DelicateWeb3Api
