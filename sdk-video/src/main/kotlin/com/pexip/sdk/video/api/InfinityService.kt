package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.RealInfinityService
import okhttp3.OkHttpClient

/**
 * A fluent client for Infinity REST API v2.
 */
public interface InfinityService {

    /**
     * Creates a new [RequestBuilder].
     *
     * @param node a conferencing node against which to perform requests
     */
    public fun newRequest(node: Node): RequestBuilder

    /**
     * Represents the (Other miscellaneous requests)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#misc] section.
     */
    public interface RequestBuilder {

        /**
         * Checks the status of the conferencing node.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#maintenance_mode].
         *
         * @return true if conferencing node is in maintenance mode, false otherwise
         */
        public fun status(): Call<Boolean>

        /**
         * Sets the conference alias.
         *
         * @param conferenceAlias a conference alias
         */
        public fun conference(conferenceAlias: ConferenceAlias): ConferenceStep
    }

    /**
     * Represents the (Conference control functions)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#conference] section.
     */
    public interface ConferenceStep {

        /**
         * Requests a token for the conference alias.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#request_token].
         *
         * @param request a request body
         * @return a token for the conference
         */
        public fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse>

        /**
         * Requests a token for the conference alias.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#request_token].
         *
         * @param request a request body
         * @param pin an optional PIN
         * @return a token for the conference
         */
        public fun requestToken(
            request: RequestTokenRequest,
            pin: String,
        ): Call<RequestTokenResponse>

        /**
         * Refreshes the token.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#refresh_token].
         *
         * @param token a current valid token
         * @return a new token for the conference
         */
        public fun refreshToken(token: String): Call<RefreshTokenResponse>

        /**
         * Releases the token.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#release_token].
         *
         * @param token a valid token
         */
        public fun releaseToken(token: String): Call<Unit>

        /**
         * Sets the participant ID.
         *
         * @param participantId an ID of the participant
         */
        public fun participant(participantId: ParticipantId): ParticipantStep
    }

    /**
     * Represents the (Participant functions)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#participant] section.
     */
    public interface ParticipantStep {

        /**
         * Requests an upgrade of the call to include media.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#calls].
         *
         * @param request a request body
         * @param token a valid token
         * @return an answer
         */
        public fun calls(request: CallsRequest, token: String): Call<CallsResponse>

        /**
         * Sets the call ID.
         *
         * @param callId an ID of the call
         */
        public fun call(callId: CallId): CallStep
    }

    /**
     * Represents the (Call functions)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#call_functions] section.
     */
    public interface CallStep {

        /**
         * Sends the new ICE candidate.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#new_candidate].
         *
         * @param request a request body
         * @param token a valid token
         */
        public fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit>

        /**
         * Acks the call.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#ack].
         *
         * @param token a valid token
         */
        public fun ack(token: String): Call<Unit>
    }

    public companion object {

        @JvmStatic
        @JvmOverloads
        public fun create(client: OkHttpClient = OkHttpClient()): InfinityService =
            RealInfinityService(client)
    }
}
