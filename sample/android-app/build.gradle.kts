/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("android-app-convention")
}

android {
    defaultConfig {
        applicationId = "dev.icerock.moko.samples.web3"

        multiDexEnabled = true
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(libs.appCompat)
    implementation(libs.material)
    implementation(libs.multidex)

    implementation(projects.sample.mppLibrary)
}
