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
package com.pexip.sdk.registration

/**
 * @property directoryEnabled whether directory is available
 * @property routeViaRegistrar whether outgoing calls should be routed using the registration's node
 */
public interface Registration {

    public val directoryEnabled: Boolean

    public val routeViaRegistrar: Boolean

    /**
     * Fetches the list of registered devices for a given search query.
     *
     * @param query a search query
     * @param callback a callback to be invoked on completion
     */
    public fun getRegisteredDevices(query: String = "", callback: RegisteredDevicesCallback)

    /**
     * Registers a [RegistrationEventListener].
     *
     * @param listener a registration event listener
     */
    public fun registerRegistrationEventListener(listener: RegistrationEventListener)

    /**
     * Unregisters a [RegistrationEventListener].
     *
     * @param listener a registration event listener
     */
    public fun unregisterRegistrationEventListener(listener: RegistrationEventListener)

    /**
     * Disposes the registration. Once disposed, the [Registration] object is no longer valid.
     */
    public fun dispose()
}
