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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.time.Year

class KotlinDokkaPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.dokka")
        val footerMessageProvider = provider {
            "${Year.now().value} Pexip® AS, All rights reserved."
        }
        tasks.withType<DokkaMultiModuleTask>().configureEach {
            includes.from("README.md")
            pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
                footerMessage = footerMessageProvider.get()
                customAssets += file("dokka/pexip.svg")
                customStyleSheets += file("dokka/logo-styles.css")
            }
        }
        val hasAndroidLibraryPluginProvider = provider {
            pluginManager.hasPlugin("com.android.library")
        }
        tasks.withType<DokkaTaskPartial>().configureEach {
            dokkaSourceSets.configureEach {
                includes.from("README.md")
                externalDocumentationLink("https://kotlin.github.io/kotlinx.coroutines/")
                noAndroidSdkLink.set(hasAndroidLibraryPluginProvider.map { !it })
            }
            pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
                footerMessage = footerMessageProvider.get()
                separateInheritedMembers = true
            }
        }
    }
}
