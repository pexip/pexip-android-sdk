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
package com.pexip.sdk.api.infinity.internal

import assertk.Table2
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationAsMillisecondsSerializerTest {

    private lateinit var json: Json
    private lateinit var serializer: KSerializer<Duration>
    private lateinit var table: Table2<Duration, String>

    @BeforeTest
    fun setUp() {
        json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        serializer = DurationAsMillisecondsSerializer
        table = tableOf("Duration", "String")
            .row(5.milliseconds, "5")
            .row(5.seconds, "5000")
            .row(5.minutes, "300000")
            .row(5.hours, "18000000")
            .row(5.days, "432000000")
            .row(Duration.ZERO, "0")
            .row(Duration.INFINITE, "${Long.MAX_VALUE}")
    }

    @Test
    fun `correctly serializes the duration`() {
        table.forAll { duration, value ->
            assertThat(json.encodeToString(serializer, duration)).isEqualTo(value)
        }
    }

    @Test
    fun `correctly deserializes the duration`() {
        table.forAll { duration, value ->
            assertThat(json.decodeFromString(serializer, value)).isEqualTo(duration)
        }
    }
}
