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
package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.internal.InfinityServiceImpl
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
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
     * @throws IllegalArgumentException if the [node] is invalid
     */
    public fun newRequest(node: URL): RequestBuilder

    /**
     * Represents the [Other miscellaneous requests](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#misc) section.
     *
     * @property infinityService the [InfinityService] that produced this [RequestBuilder]
     */
    public interface RequestBuilder {

        public val infinityService: InfinityService

        /**
         * Checks the status of the conferencing node.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#maintenance_mode).
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

        /**
         * Sets the registration alias.
         *
         * @param deviceAlias a registration alias
         */
        public fun registration(deviceAlias: String): RegistrationStep
    }

    /**
     * Represents the [Conference control functions](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#conference) section.
     *
     * @property requestBuilder the [RequestBuilder] that produced this [ConferenceStep]
     */
    public interface ConferenceStep {

        public val requestBuilder: RequestBuilder

        /**
         * Requests a token for the conference alias.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#request_token).
         *
         * @param request a request body
         * @return a token for the conference
         */
        public fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse>

        /**
         * Requests a token for the conference alias.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#request_token).
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
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#refresh_token).
         *
         * @param token a current valid token
         * @return a new token for the conference
         */
        public fun refreshToken(token: String): Call<RefreshTokenResponse>

        /**
         * Refreshes the token.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#refresh_token).
         *
         * @param token a current valid token
         * @return a new token for the conference
         */
        public fun refreshToken(token: Token): Call<RefreshTokenResponse>

        /**
         * Releases the token.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#release_token).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun releaseToken(token: String): Call<Boolean>

        /**
         * Releases the token.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#release_token).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun releaseToken(token: Token): Call<Boolean>

        /**
         * Sends a message to all participants in the conference.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#message).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun message(request: MessageRequest, token: String): Call<Boolean>

        /**
         * Sends a message to all participants in the conference.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#message).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun message(request: MessageRequest, token: Token): Call<Boolean>

        /**
         * Provides the theme resources of the conference (direct media only). Used in conjunction
         * with the **splash_screen** server event, the relevant theme resources can be used to
         * locally render a particular splash screen on the client.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#theme).
         *
         * @param token a valid token
         * @return a [Map] of [SplashScreenResponse]
         */
        public fun theme(token: String): Call<Map<String, SplashScreenResponse>>

        /**
         * Provides the theme resources of the conference (direct media only). Used in conjunction
         * with the **splash_screen** server event, the relevant theme resources can be used to
         * locally render a particular splash screen on the client.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#theme).
         *
         * @param token a valid token
         * @return a [Map] of [SplashScreenResponse]
         */
        public fun theme(token: Token): Call<Map<String, SplashScreenResponse>>

        /**
         * Creates a URL that points to a specific theme resource, such as an image.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#theme).
         *
         * @param token a valid token
         * @return a URL of the resource
         */
        public fun theme(path: String, token: String): String

        /**
         * Creates a URL that points to a specific theme resource, such as an image.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#theme).
         *
         * @param token a valid token
         * @return a URL of the resource
         */
        public fun theme(path: String, token: Token): String

        /**
         * Subscribes to server-side events.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#server_sent).
         *
         * @param token a valid token
         */
        public fun events(token: String): EventSourceFactory

        /**
         * Subscribes to server-side events.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#server_sent).
         *
         * @param token a valid token
         */
        public fun events(token: Token): EventSourceFactory

        /**
         * Sets the participant ID.
         *
         * @param participantId an ID of the participant
         */
        public fun participant(participantId: UUID): ParticipantStep
    }

    /**
     * Represents the registration control functions section.
     *
     * @property requestBuilder the [RequestBuilder] that produced this [RegistrationStep]
     */
    public interface RegistrationStep {

        public val requestBuilder: RequestBuilder

        /**
         * Requests a token for the registration alias.
         *
         * @param username a username
         * @param password a password
         * @throws IllegalArgumentException if either username or password is blank
         * @throws NoSuchRegistrationException if device alias does not exist or username or password is not correct
         * @throws NoSuchNodeException if the node does not exist
         * @throws IllegalStateException on server error
         * @return a registration token
         */
        public fun requestToken(
            username: String,
            password: String,
        ): Call<RequestRegistrationTokenResponse>

        /**
         * Refreshes the token.
         *
         * @param token a current valid token
         * @throws IllegalArgumentException if token is blank
         * @throws NoSuchRegistrationException if device alias does not exist
         * @throws InvalidTokenException if token is not valid
         * @throws NoSuchNodeException if the node does not exist
         * @throws IllegalStateException on server error
         * @return a new registration token
         */
        public fun refreshToken(token: String): Call<RefreshRegistrationTokenResponse>

        /**
         * Refreshes the token.
         *
         * @param token a current valid token
         * @throws IllegalArgumentException if token is blank
         * @throws NoSuchRegistrationException if device alias does not exist
         * @throws InvalidTokenException if token is not valid
         * @throws NoSuchNodeException if the node does not exist
         * @throws IllegalStateException on server error
         * @return a new registration token
         */
        public fun refreshToken(token: Token): Call<RefreshRegistrationTokenResponse>

        /**
         * Releases the token.
         *
         * @param token a valid token
         * @throws IllegalArgumentException if token is blank
         * @throws NoSuchRegistrationException if device alias does not exist
         * @throws InvalidTokenException if token is not valid
         * @throws NoSuchNodeException if the node does not exist
         * @throws IllegalStateException on server error
         * @return true if operation was successful, false otherwise
         */
        public fun releaseToken(token: String): Call<Boolean>

        /**
         * Releases the token.
         *
         * @param token a valid token
         * @throws IllegalArgumentException if token is blank
         * @throws NoSuchRegistrationException if device alias does not exist
         * @throws InvalidTokenException if token is not valid
         * @throws NoSuchNodeException if the node does not exist
         * @throws IllegalStateException on server error
         * @return true if operation was successful, false otherwise
         */
        public fun releaseToken(token: Token): Call<Boolean>

        /**
         * Subscribes to server-side events.
         *
         * @param token a valid token
         * @throws IllegalArgumentException if token is blank
         * @return an event source factory
         */
        public fun events(token: String): EventSourceFactory

        /**
         * Subscribes to server-side events.
         *
         * @param token a valid token
         * @throws IllegalArgumentException if token is blank
         * @return an event source factory
         */
        public fun events(token: Token): EventSourceFactory

        /**
         * Returns a list of registrations.
         *
         * @param token a valid token
         * @param query a search query
         * @throws IllegalArgumentException if token is blank
         * @return a list of registrations
         */
        public fun registrations(
            token: String,
            query: String = "",
        ): Call<List<RegistrationResponse>>

        /**
         * Returns a list of registrations.
         *
         * @param token a valid token
         * @param query a search query
         * @throws IllegalArgumentException if token is blank
         * @return a list of registrations
         */
        public fun registrations(token: Token, query: String = ""): Call<List<RegistrationResponse>>
    }

    /**
     * Represents the [Participant functions](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#participant) section.
     *
     * @property conferenceStep the [ConferenceStep] that produced this [ParticipantStep]
     */
    public interface ParticipantStep {

        public val conferenceStep: ConferenceStep

        /**
         * Requests an upgrade of the call to include media.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#calls).
         *
         * @param request a request body
         * @param token a valid token
         * @return an answer
         */
        public fun calls(request: CallsRequest, token: String): Call<CallsResponse>

        /**
         * Requests an upgrade of the call to include media.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#calls).
         *
         * @param request a request body
         * @param token a valid token
         * @return an answer
         */
        public fun calls(request: CallsRequest, token: Token): Call<CallsResponse>

        /**
         * Sends DTMF digits to the participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#dtmf).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun dtmf(request: DtmfRequest, token: String): Call<Boolean>

        /**
         * Sends DTMF digits to the participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#dtmf).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun dtmf(request: DtmfRequest, token: Token): Call<Boolean>

        /**
         * Requests to mute participant's audio.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#mute).
         *
         * @param token a valid token
         */
        public fun mute(token: String): Call<Unit>

        /**
         * Requests to mute participant's audio.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#mute).
         *
         * @param token a valid token
         */
        public fun mute(token: Token): Call<Unit>

        /**
         * Requests to unmute participant's audio.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#mute).
         *
         * @param token a valid token
         */
        public fun unmute(token: String): Call<Unit>

        /**
         * Requests to unmute participant's audio.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#mute).
         *
         * @param token a valid token
         */
        public fun unmute(token: Token): Call<Unit>

        /**
         * Requests to mute participant's video.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#videomute).
         *
         * @param token a valid token
         */
        public fun videoMuted(token: String): Call<Unit>

        /**
         * Requests to mute participant's video.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#videomute).
         *
         * @param token a valid token
         */
        public fun videoMuted(token: Token): Call<Unit>

        /**
         * Requests to unmute participant's video.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#videomute).
         *
         * @param token a valid token
         */
        public fun videoUnmuted(token: String): Call<Unit>

        /**
         * Requests to unmute participant's video.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#videomute).
         *
         * @param token a valid token
         */
        public fun videoUnmuted(token: Token): Call<Unit>

        /**
         * Requests to take presentation floor.
         *
         * @param token a valid token
         */
        public fun takeFloor(token: String): Call<Unit>

        /**
         * Requests to take presentation floor.
         *
         * @param token a valid token
         */
        public fun takeFloor(token: Token): Call<Unit>

        /**
         * Requests to release presentation floor.
         *
         * @param token a valid token
         */
        public fun releaseFloor(token: String): Call<Unit>

        /**
         * Requests to release presentation floor.
         *
         * @param token a valid token
         */
        public fun releaseFloor(token: Token): Call<Unit>

        /**
         * Sends a message to this participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#message).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun message(request: MessageRequest, token: String): Call<Boolean>

        /**
         * Sends a message to this participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#message).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun message(request: MessageRequest, token: Token): Call<Boolean>

        /**
         * Specifies the aspect ratio the participant would like to receive.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#preferred_aspect_ratio).
         *
         * @param request a request body
         * @param token a valid token
         */
        public fun preferredAspectRatio(
            request: PreferredAspectRatioRequest,
            token: String,
        ): Call<Boolean>

        /**
         * Specifies the aspect ratio the participant would like to receive.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#preferred_aspect_ratio).
         *
         * @param request a request body
         * @param token a valid token
         */
        public fun preferredAspectRatio(
            request: PreferredAspectRatioRequest,
            token: Token,
        ): Call<Boolean>

        /**
         * Sets the call ID.
         *
         * @param callId an ID of the call
         */
        public fun call(callId: UUID): CallStep
    }

    /**
     * Represents the [Call functions](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#call_functions) section.
     *
     * @property participantStep the [ParticipantStep] that produced this [CallStep]
     */
    public interface CallStep {

        public val participantStep: ParticipantStep

        /**
         * Sends the new ICE candidate.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#new_candidate).
         *
         * @param request a request body
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit>

        /**
         * Sends the new ICE candidate.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#new_candidate).
         *
         * @param request a request body
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun newCandidate(request: NewCandidateRequest, token: Token): Call<Unit>

        /**
         * Acks the call.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#ack).
         *
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun ack(token: String): Call<Unit>

        /**
         * Acks the call.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#ack).
         *
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun ack(token: Token): Call<Unit>

        /**
         * Acks the call.
         *
         * This is only used for direct media calls and should contain the local SDP.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#ack).
         *
         * @param request a request body
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun ack(request: AckRequest, token: String): Call<Unit>

        /**
         * Acks the call.
         *
         * This is only used for direct media calls and should contain the local SDP.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#ack).
         *
         * @param request a request body
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         */
        public fun ack(request: AckRequest, token: Token): Call<Unit>

        /**
         * Sends a new SDP.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#update).
         *
         * @param request a request body
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         * @return a new SDP
         */
        public fun update(request: UpdateRequest, token: String): Call<UpdateResponse>

        /**
         * Sends a new SDP.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#update).
         *
         * @param request a request body
         * @param token a valid token
         * @throws InvalidTokenException if the token is invalid
         * @throws NoSuchNodeException if the node doesn't exist
         * @throws NoSuchConferenceException if the conference doesn't exist
         * @return a new SDP
         */
        public fun update(request: UpdateRequest, token: Token): Call<UpdateResponse>

        /**
         * Sends DTMF digits to the participant (gateway call only).
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#call_dtmf).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun dtmf(request: DtmfRequest, token: String): Call<Boolean>

        /**
         * Sends DTMF digits to the participant (gateway call only).
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#call_dtmf).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun dtmf(request: DtmfRequest, token: Token): Call<Boolean>
    }

    public companion object {

        internal val Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            serializersModule = SerializersModule {
                polymorphicDefaultDeserializer(ElementResponse::class) {
                    ElementResponse.Unknown.serializer()
                }
                polymorphicDefaultDeserializer(DataChannelMessage::class) {
                    DataChannelMessage.Unknown.serializer()
                }
            }
        }

        @JvmStatic
        @JvmOverloads
        public fun create(client: OkHttpClient = OkHttpClient()): InfinityService = create(
            client = client,
            json = Json,
        )

        internal fun create(client: OkHttpClient, json: Json): InfinityService =
            InfinityServiceImpl(client, json)
    }
}