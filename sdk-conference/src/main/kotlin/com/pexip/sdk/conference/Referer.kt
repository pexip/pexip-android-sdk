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
package com.pexip.sdk.conference

/**
 * Handles call transfer.
 */
public interface Referer {

    /**
     * Transfers the call using information specified in the [event].
     *
     * Note that upon successful call transfer the current [Conference] will still be active and
     * must be disposed of manually.
     *
     * @param event a [ReferConferenceEvent] that contains information about target conference
     * @return a new [Conference]
     * @throws [ReferException] if [refer] did not succeed
     */
    public suspend fun refer(event: ReferConferenceEvent): Conference
}
