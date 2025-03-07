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

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.vanniktech.maven.publish")
        group = checkNotNull(property("group")) { "group == null." }
        version = checkNotNull(property("version")) { "version == null." }
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(host = SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
            signAllPublications()
            pom {
                name.set(target.name)
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
    }
}
