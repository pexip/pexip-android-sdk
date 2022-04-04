package com.pexip.sdk.api

/**
 * Communicates responses from a server or offline requests. One and only one method will be invoked
 * in response to a given request.
 *
 * @param T Successful response body type.
 */
public interface Callback<T> {

    /**
     * Invoked for a received response.
     */
    public fun onSuccess(call: Call<T>, response: T)

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    public fun onFailure(call: Call<T>, t: Throwable)
}
