package com.pexip.sdk.api

/**
 * Represents an API call.
 */
public interface Call<T> {

    /**
     * Executes the call on the caller thread.
     */
    public fun execute(): T

    /**
     * Schedules the request to be executed at some point in the future.
     *
     * @param callback a callback to invoke on completion
     */
    public fun enqueue(callback: Callback<T>)

    /**
     * Cancels the request, if possible.
     */
    public fun cancel()
}
