/*
 * Copyright 2023 Pexip AS
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

import com.android.build.api.dsl.ApplicationExtension
import com.pexip.sdk.internal.Android
import com.pexip.sdk.internal.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

@Suppress("UnstableApiUsage")
class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        configure<ApplicationExtension> {
            compileSdk = Android.compileSdk
            defaultConfig {
                minSdk = Android.minSdk
                targetSdk = Android.targetSdk
                testInstrumentationRunner = Android.testInstrumentationRunner
            }
            buildFeatures {
                buildConfig = true
            }
            compileOptions {
                sourceCompatibility = Android.sourceCompatibility
                targetCompatibility = Android.targetCompatibility
            }
            composeOptions {
                kotlinCompilerExtensionVersion = libs.findVersion("androidx-compose-compiler")
                    .map(VersionConstraint::toString)
                    .get()
            }
            testOptions {
                unitTests {
                    isIncludeAndroidResources = true
                }
            }
            lint {
                checkDependencies = true
                ignoreTestSources = true
            }
            packaging {
                resources.excludes += "META-INF/{AL2.0,LGPL2.1}"
            }
        }
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(11))
            }
        }
        tasks.withType<Test>().configureEach {
            testLogging.exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
