/*
 * Copyright 2023-2025 Pexip AS
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import java.time.Year

class KotlinDokkaPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.dokka")
            apply("org.jetbrains.dokka-javadoc")
        }
        val hasAndroidLibraryPluginProvider = provider {
            pluginManager.hasPlugin("com.android.library")
        }
        val footerMessageProvider = provider {
            "${Year.now().value} Pexip® AS, All rights reserved."
        }
        extensions.configure(DokkaExtension::class.java) {
            dokkaSourceSets.configureEach {
                if (name in setOf("main, jvmMain")) {
                    samples.from("src/test/kotlin/Samples.kt")
                }
                includes.from("README.md")
                enableAndroidDocumentationLink.set(hasAndroidLibraryPluginProvider.map { it })
                sourceLink {
                    remoteUrl("https://github.com/pexip/pexip-android-sdk/blob/main/")
                    localDirectory.set(rootDir)
                }
            }
            pluginsConfiguration.named("html", DokkaHtmlPluginParameters::class.java) {
                customAssets.from("$rootDir/dokka/pexip.svg")
                customStyleSheets.from("$rootDir/dokka/logo-styles.css")
                footerMessage.set(footerMessageProvider)
            }
        }
    }
}
