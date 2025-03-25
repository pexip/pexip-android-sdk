/*
 * Copyright 2022-2025 Pexip AS
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
package com.pexip.sdk.sample.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.okio.OkioSerializer
import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import okio.BufferedSink
import okio.BufferedSource
import okio.IOException

@OptIn(ExperimentalSerializationApi::class)
object SettingsSerializer : OkioSerializer<Settings> {

    override val defaultValue: Settings
        get() = Settings()

    override suspend fun readFrom(source: BufferedSource): Settings = runInterruptible {
        try {
            Json.Default.decodeFromBufferedSource(source)
        } catch (e: IOException) {
            throw CorruptionException("Cannot read json.", e)
        }
    }

    override suspend fun writeTo(t: Settings, sink: BufferedSink) = runInterruptible {
        Json.Default.encodeToBufferedSink(t, sink)
    }
}
