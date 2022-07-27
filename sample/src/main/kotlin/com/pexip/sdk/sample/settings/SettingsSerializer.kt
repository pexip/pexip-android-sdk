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
