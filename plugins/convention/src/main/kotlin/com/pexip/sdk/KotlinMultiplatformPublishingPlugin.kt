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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class KotlinMultiplatformPublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(KotlinMultiplatformPlugin::class)
            apply(KotlinDokkaPlugin::class)
            apply(LicenseePlugin::class)
            apply(PublishingPlugin::class)
        }
        configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                val name = name
                val javadocJar = tasks.register<Jar>("${name}JavadocJar") {
                    from(tasks.named("dokkaJavadoc"))
                    archiveClassifier.set("javadoc")
                    archiveBaseName.set("${archiveBaseName.get()}-$name")
                }
                artifact(javadocJar)
            }
        }
    }
}
