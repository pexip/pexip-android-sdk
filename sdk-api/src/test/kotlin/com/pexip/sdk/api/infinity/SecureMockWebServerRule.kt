/*
 * Copyright 2024-2025 Pexip AS
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
package com.pexip.sdk.api.infinity

import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.rules.ExternalResource
import java.net.InetAddress

class SecureMockWebServerRule : ExternalResource() {

    val server = MockWebServer()

    lateinit var client: OkHttpClient
        private set

    override fun before() {
        val localhost = InetAddress.getByName("localhost")
        val certificate = HeldCertificate.Builder()
            .addSubjectAlternativeName(localhost.canonicalHostName)
            .build()
        val serverCertificate = HandshakeCertificates.Builder()
            .heldCertificate(certificate)
            .build()
        val clientCertificate = HandshakeCertificates.Builder()
            .addTrustedCertificate(certificate.certificate)
            .build()
        server.useHttps(
            sslSocketFactory = serverCertificate.sslSocketFactory(),
        )
        server.start(inetAddress = localhost, port = 0)
        client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificate.sslSocketFactory(), clientCertificate.trustManager)
            .build()
    }

    override fun after() {
        server.close()
    }
}
