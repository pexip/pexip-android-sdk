package com.pexip.sdk.registration

public interface RegisteredDevicesCallback {

    /**
     * Invoked for a received list of registered devices.
     *
     * @param devices a list of registered devices
     */
    public fun onSuccess(devices: List<RegisteredDevice>)

    /**
     * Invoked when a network exception occurred when fetching the list or an unexpected exception
     * occurred.
     *
     * @param t an exception that occurred
     */
    public fun onFailure(t: Throwable)
}
