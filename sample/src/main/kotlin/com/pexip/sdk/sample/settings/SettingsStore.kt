package com.pexip.sdk.sample.settings

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsStore @Inject constructor(private val store: DataStore<Settings>) {

    fun getDisplayName(): Flow<String> = store.data
        .map { it.display_name }
        .distinctUntilChanged()

    suspend fun setDisplayName(displayName: String) {
        store.updateData { it.copy(display_name = displayName.trim()) }
    }
}
