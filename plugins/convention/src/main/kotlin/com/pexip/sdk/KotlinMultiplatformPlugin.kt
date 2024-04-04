/*
 * Copyright 2023-2024 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk

import com.pexip.sdk.internal.assertk
import com.pexip.sdk.internal.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

class KotlinMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.lint")
        }
        with(kotlinExtension) {
            explicitApi()
            jvmToolchain(11)
            sourceSets.named("commonTest") {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(libs.assertk)
                }
            }
        }
        tasks.withType<KotlinCompileCommon> {
            compilerOptions {
                freeCompilerArgs.add("-Xjvm-default=all")
            }
        }
        tasks.withType<Test> {
            testLogging.exceptionFormat = TestExceptionFormat.FULL
            systemProperty("kotlinx.coroutines.stacktrace.recovery", false)
        }
    }
}
