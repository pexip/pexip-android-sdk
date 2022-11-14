package com.pexip.sdk.registration

public interface Registration {

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
