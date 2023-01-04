package com.pexip.sdk.sample.di

import android.app.Application
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.pexip.sdk.sample.settings.SettingsSerializer
import com.pexip.sdk.sample.settings.SettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsStoreModule {

    @Provides
    @Singleton
    fun Application.provideSettingsStore(): SettingsStore {
        val store = DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = { dataStoreFile("settings.pb") },
        )
        return SettingsStore(store)
    }
}
