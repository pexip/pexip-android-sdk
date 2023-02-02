/*
 * Copyright 2022 Pexip AS
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
import androidx.datastore.core.Serializer
import kotlinx.coroutines.runInterruptible
import okio.IOException
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings
        get() = Settings()

    override suspend fun readFrom(input: InputStream): Settings = try {
        runInterruptible { Settings.ADAPTER.decode(input) }
    } catch (e: IOException) {
        throw CorruptionException("Cannot read proto.", e)
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        runInterruptible { Settings.ADAPTER.encode(output, t) }
    }
}
