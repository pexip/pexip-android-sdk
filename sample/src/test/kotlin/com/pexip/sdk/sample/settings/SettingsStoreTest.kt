package com.pexip.sdk.sample.settings

import androidx.datastore.core.DataStoreFactory
import app.cash.turbine.testIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsStoreTest {

    @get:Rule
    val rule = TemporaryFolder()

    private lateinit var store: SettingsStore

    @BeforeTest
    fun setUp() {
        val dataStore = DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = rule::newFile,
        )
        store = SettingsStore(dataStore)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setDisplayName updates display_name`() = runTest {
        val turbine = store.getDisplayName().testIn(this)
        assertEquals("", turbine.awaitItem())
        repeat(10) {
            val displayName = "   $it  "
            store.setDisplayName(displayName)
            assertEquals(displayName.trim(), turbine.awaitItem())
        }
        turbine.cancelAndIgnoreRemainingEvents()
    }
}
