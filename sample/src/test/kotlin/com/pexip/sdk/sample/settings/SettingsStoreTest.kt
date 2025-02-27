/*
 * Copyright 2022-2024 Pexip AS
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

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toOkioPath
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
        store = SettingsStore { rule.newFolder().toOkioPath() / "settings.json" }
    }

    @Test
    fun `setDisplayName updates display_name`() = runTest {
        store.getDisplayName().test {
            assertEquals("", awaitItem())
            repeat(10) {
                val displayName = "   $it  "
                store.setDisplayName(displayName)
                assertEquals(displayName.trim(), awaitItem())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
