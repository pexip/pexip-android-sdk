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
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("maven-publish")
            apply("signing")
        }
        group = checkNotNull(property("group")) { "group == null." }
        version = checkNotNull(property("version")) { "version == null." }
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "MavenCentral"
                    val repositoryUrl = when (version.toString().endsWith("SNAPSHOT")) {
                        true -> "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                        else -> "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    }
                    setUrl(repositoryUrl)
                    credentials(PasswordCredentials::class)
                }
            }
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set(artifactId)
                    url.set("https://github.com/pexip/pexip-android-sdk")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("pexip")
                            organization.set("Pexip AS")
                            organizationUrl.set("https://www.pexip.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/pexip/pexip-android-sdk.git")
                        developerConnection.set("scm:git:ssh://github.com:pexip/pexip-android-sdk.git")
                        url.set("https://github.com/pexip/pexip-android-sdk")
                    }
                }
            }
            configure<SigningExtension> {
                val signingKey: String? by project
                val signingPassword: String? by project
                useInMemoryPgpKeys(signingKey, signingPassword)
                if (providers.environmentVariable("CI").isPresent) {
                    sign(publications)
                }
            }
        }
    }
}
