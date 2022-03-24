/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("publication-convention")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    commonMainImplementation(libs.coroutines)
    commonMainImplementation(libs.kbignum)
    commonMainImplementation(libs.kotlinSerialization)
    commonMainImplementation(libs.klock)
    commonMainImplementation(libs.ktorClient)
    commonMainImplementation(libs.ktorClientLogigng)
    commonMainImplementation(libs.ktorWebsockets)
    
    commonTestImplementation(libs.kotlinTestCommon)
    commonTestImplementation(libs.kotlinTestAnnotations)
    commonTestImplementation(libs.ktorClientMock)

    jvmMainImplementation(libs.ktorClientOkHttp)
    jvmTestImplementation(libs.kotlinTest)
    jvmTestImplementation(libs.kotlinTestJunit)

    iosMainImplementation(libs.ktorClientIos)
    iosTestImplementation(libs.ktorClientIos)
}

// now standard test task use --standalone but it broke network calls
val newTestTask = tasks.create("iosX64TestWithNetwork") {
    val linkTask = tasks.getByName("linkDebugTestIosX64") as org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
    dependsOn(linkTask)

    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Runs tests for target 'ios' on an iOS simulator"

    doLast {
        val binary = linkTask.binary.outputFile
        val device = "iPhone 8"
        exec {
            commandLine = listOf("xcrun", "simctl", "boot", device)
            isIgnoreExitValue = true
        }
        exec {
            commandLine = listOf(
                "xcrun",
                "simctl",
                "spawn",
                device,
                binary.absolutePath
            )
        }
        exec {
            commandLine = listOf("xcrun", "simctl", "shutdown", device)
        }
    }
}

with(tasks.getByName("iosX64Test")) {
    dependsOn(newTestTask)
    onlyIf { false }
}
