/*
 * Copyright 2022-2023 Pexip AS
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
package com.pexip.sdk.sample.alias

import com.pexip.sdk.sample.asWorker
import com.pexip.sdk.sample.pinrequirement.PinRequirementProps
import com.squareup.workflow1.Worker
import com.squareup.workflow1.transform
import com.squareup.workflow1.ui.TextController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class AliasState(
    val alias: TextController = TextController(),
    val host: TextController = TextController(),
    val blankAlias: Boolean = alias.textValue.isBlank(),
    val blankAliasWorker: Worker<Boolean> = alias.asWorker().transform {
        it.map(String::isBlank).distinctUntilChanged()
    },
    val presentationInMain: Boolean = false,
    val pinRequirementProps: PinRequirementProps? = null,
)
