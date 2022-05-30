package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.internal.RealInfinityService
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.net.URL
import java.util.UUID

/**
 * A fluent client for Infinity REST API v2.
 */
public interface InfinityService {

    /**
     * Creates a new [RequestBuilder].
     *
     * @param node a conferencing node against which to perform requests
     */
    public fun newRequest(node: URL): RequestBuilder

    /**
     * Represents the (Other miscellaneous requests)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#misc] section.
     */
    public interface RequestBuilder {

        /**
         * Checks the status of the conferencing node.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#maintenance_mode].
         *
         * @return true if the node is available, false otherwise
         * @throws NoSuchNodeException if the node doesn't exist
         */
        public fun status(): Call<Boolean>

        /**
         * Sets the conference alias.
         *
         * @param conferenceAlias a conference alias
         */
        public fun conference(conferenceAlias: String): ConferenceStep
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
         * Sends a message to all participants in the conference.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm#message].
         *
         * @param request a request body
         * @param token a valid token
         */
        public fun message(request: MessageRequest, token: String): Call<Boolean>

        /**
         * Subscribes to server-side events.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#server_sent].
         *
         * @param token a valid token
         */
        public fun events(token: String): EventSourceFactory

        /**
         * Sets the participant ID.
         *
         * @param participantId an ID of the participant
         */
        public fun participant(participantId: UUID): ParticipantStep
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
         * Sends DTMF digits to the participant.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#dtmf].
         *
         * @param request a request body
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun dtmf(request: DtmfRequest, token: String): Call<Boolean>

        /**
         * Requests to mute participant's audio.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm#mute].
         *
         * @param token a valid token
         */
        public fun mute(token: String): Call<Unit>

        /**
         * Requests to unmute participant's audio.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm#mute].
         *
         * @param token a valid token
         */
        public fun unmute(token: String): Call<Unit>

        /**
         * Requests to mute participant's video.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm#videomute].
         *
         * @param token a valid token
         */
        public fun videoMuted(token: String): Call<Unit>

        /**
         * Requests to unmute participant's video.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm#videomute].
         *
         * @param token a valid token
         */
        public fun videoUnmuted(token: String): Call<Unit>

        /**
         * Sets the call ID.
         *
         * @param callId an ID of the call
         */
        public fun call(callId: UUID): CallStep
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
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit>

        /**
         * Acks the call.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#ack].
         *
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun ack(token: String): Call<Unit>

        /**
         * Sends a new SDP.
         *
         * See (documentation)[https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#update].
         *
         * @param request a request body
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         * @return a new SDP
         */
        public fun update(request: UpdateRequest, token: String): Call<UpdateResponse>
    }

    public companion object {

        @JvmStatic
        @JvmOverloads
        public fun create(client: OkHttpClient = OkHttpClient()): InfinityService = create(
            client = client,
            json = Json { ignoreUnknownKeys = true }
        )

        internal fun create(client: OkHttpClient, json: Json): InfinityService =
            RealInfinityService(client, json)
    }
}
