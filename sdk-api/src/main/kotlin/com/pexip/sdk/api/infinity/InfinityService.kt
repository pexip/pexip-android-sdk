/*
 * Copyright 2022-2024 Pexip AS
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
@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.internal.InfinityServiceImpl
import com.pexip.sdk.infinity.BreakoutId
import com.pexip.sdk.infinity.CallId
import com.pexip.sdk.infinity.LayoutId
import com.pexip.sdk.infinity.Node
import com.pexip.sdk.infinity.ParticipantId
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import okhttp3.OkHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit

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
    public fun newRequest(node: Node): RequestBuilder = throw NotImplementedError()

    /**
     * Creates a new [RequestBuilder].
     *
     * @param node a conferencing node against which to perform requests
     * @throws IllegalArgumentException if the [node] is invalid
     */
    @Deprecated(
        message = "Superseded by a variant that accepts an instance of Node.",
        level = DeprecationLevel.ERROR,
    )
    public fun newRequest(node: URL): RequestBuilder = throw NotImplementedError()

    /**
     * Represents the [Other miscellaneous requests](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#misc) section.
     */
    public interface RequestBuilder {

        /**
         * The Infinity service that produced this request builder.
         */
        @Deprecated("Deprecated without a replacement.")
        public val infinityService: InfinityService
            get() = throw NotImplementedError()

        /**
         * A node that this request builder will use.
         */
        public val node: Node
            get() = throw NotImplementedError()

        /**
         * Checks the status of the conferencing node.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#maintenance_mode).
         *
         * @return true if the node is available, false otherwise
         * @throws NoSuchNodeException if the node doesn't exist
         */
        public fun status(): Call<Boolean> = throw NotImplementedError()

        /**
         * Sets the conference alias.
         *
         * @param conferenceAlias a conference alias
         */
        public fun conference(conferenceAlias: String): ConferenceStep = throw NotImplementedError()

        /**
         * Sets the registration alias.
         *
         * @param deviceAlias a registration alias
         */
        public fun registration(deviceAlias: String): RegistrationStep = throw NotImplementedError()
    }

    /**
     * Represents the [Conference control functions](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#conference) section.
     */
    public interface ConferenceStep {

        /**
         * The request builder that produced this conference step.
         */
        @Deprecated("Deprecated without a replacement.")
        public val requestBuilder: RequestBuilder
            get() = throw NotImplementedError()

        /**
         * A conference alias that this conference step will use.
         */
        public val conferenceAlias: String
            get() = throw NotImplementedError()

        /**
         * Requests a token for the conference alias.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#request_token).
         *
         * @param request a request body
         * @return a token for the conference
         */
        public fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> =
            throw NotImplementedError()

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
        ): Call<RequestTokenResponse> = throw NotImplementedError()

        /**
         * Refreshes the token.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#refresh_token).
         *
         * @param token a current valid token
         * @return a new token for the conference
         */
        public fun refreshToken(token: Token): Call<RefreshTokenResponse> =
            throw NotImplementedError()

        /**
         * Releases the token.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#release_token).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun releaseToken(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Sends a message to all participants in the conference.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#message).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun message(request: MessageRequest, token: Token): Call<Boolean> =
            throw NotImplementedError()

        /**
         * This returns a list of all available layouts for the given conference.
         *
         * This includes the inbuilt layouts plus any custom layouts available on this conference.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#available_layouts).
         *
         * @param token a valid token
         * @return a list of available layouts
         */
        public fun availableLayouts(token: Token): Call<Set<LayoutId>> = throw NotImplementedError()

        /**
         * This provides all SVG representations of the layouts that are active
         * on the given conference.
         *
         * See [documentation](https://docs.pexip.com/beta/api_client/api_rest.htm#layout_svgs).
         *
         * @param token a valid token
         * @return a collection of SVG representations
         */
        public fun layoutSvgs(token: Token): Call<Map<LayoutId, String>> =
            throw NotImplementedError()

        /**
         * This request changes the conference layout, controls streaming content,
         * and enables/disables indicators and overlay text.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#transform_layout).
         *
         * @param request a request containing changes to apply to the conference layout
         * @param token a valid token
         * @throws IllegalLayoutTransformException if the requested transformation is not supported
         * @return true if operation was successful, false otherwise
         */
        public fun transformLayout(request: TransformLayoutRequest, token: Token): Call<Boolean> =
            throw NotImplementedError()

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
        public fun theme(token: Token): Call<Map<String, SplashScreenResponse>> =
            throw NotImplementedError()

        /**
         * Creates a URL that points to a specific theme resource, such as an image.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#theme).
         *
         * @param token a valid token
         * @return a URL of the resource
         */
        public fun theme(path: String, token: Token): String = throw NotImplementedError()

        /**
         * Locks the conference.
         *
         * When a conference is locked, participants waiting to join are held at
         * a "Waiting for Host" screen. These settings are only available to conference hosts.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#lock).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun lock(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Unlocks the conference.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#lock).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun unlock(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Mutes all guests in a conference.
         *
         * When muted, no guest participants can speak unless they are explicitly unmuted.
         * This setting is only available to conference hosts.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#muteguests).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun muteGuests(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Unmutes all guests in a conference.
         *
         * When unmuted, all guests on a conference can speak. This setting is only available
         * to conference hosts.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#muteguests).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun unmuteGuests(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Configure whether or not guests can unmute themselves when they have been muted by a host
         * (either directly muted or via [muteGuests]).
         *
         * See [documentation](https://docs.pexip.com/beta/api_client/api_rest.htm#set_guests_can_unmute)
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun setGuestsCanUnmute(
            request: SetGuestCanUnmuteRequest,
            token: Token,
        ): Call<Boolean> = throw NotImplementedError()

        /**
         * Lowers all raised hands.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#clearallbuzz).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun clearAllBuzz(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Disconnects all conference participants, including the participant calling the function.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#disconnect_all).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun disconnect(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Subscribes to server-side events.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#server_sent).
         *
         * @param token a valid token
         */
        public fun events(token: Token): EventSourceFactory = throw NotImplementedError()

        /**
         * Sets the conference alias.
         *
         * @param conferenceAlias a conference alias
         */
        public fun conference(conferenceAlias: String): ConferenceStep = throw NotImplementedError()

        /**
         * Sets the breakout ID.
         *
         * @param breakoutId an ID of the breakout
         */
        public fun breakout(breakoutId: BreakoutId): BreakoutStep = throw NotImplementedError()

        /**
         * Sets the participant ID.
         *
         * @param participantId an ID of the participant
         */
        public fun participant(participantId: ParticipantId): ParticipantStep =
            throw NotImplementedError()
    }

    /**
     * Represents the [Breakout room functions](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#breakout_functions) section.
     */
    public interface BreakoutStep {

        /**
         * A breakout ID that this breakout step will use.
         */
        public val breakoutId: BreakoutId
            get() = throw NotImplementedError()

        /**
         * Sets the participant ID.
         *
         * @param participantId an ID of the participant
         */
        public fun participant(participantId: ParticipantId): ParticipantStep =
            throw NotImplementedError()
    }

    /**
     * Represents the registration control functions section.
     */
    public interface RegistrationStep {

        /**
         * The request builder that produced this registration step.
         */
        @Deprecated("Deprecated without a replacement.")
        public val requestBuilder: RequestBuilder
            get() = throw NotImplementedError()

        /**
         * The device alias that this registration step will use.
         */
        public val deviceAlias: String
            get() = throw NotImplementedError()

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
        ): Call<RequestRegistrationTokenResponse> = throw NotImplementedError()

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
        public fun refreshToken(token: Token): Call<RefreshRegistrationTokenResponse> =
            throw NotImplementedError()

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
        public fun releaseToken(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Subscribes to server-side events.
         *
         * @param token a valid token
         * @throws IllegalArgumentException if token is blank
         * @return an event source factory
         */
        public fun events(token: Token): EventSourceFactory = throw NotImplementedError()

        /**
         * Returns a list of registrations.
         *
         * @param token a valid token
         * @param query a search query
         * @throws IllegalArgumentException if token is blank
         * @return a list of registrations
         */
        public fun registrations(
            token: Token,
            query: String = "",
        ): Call<List<RegistrationResponse>> = throw NotImplementedError()
    }

    /**
     * Represents the [Participant functions](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#participant) section.
     *
     * @property conferenceStep the [ConferenceStep] that produced this [ParticipantStep]
     */
    public interface ParticipantStep {

        /**
         * The conference step that produced this participant step.
         */
        @Deprecated("Deprecated without a replacement.")
        public val conferenceStep: ConferenceStep
            get() = throw NotImplementedError()

        /**
         * A participant ID that this participant step will use.
         */
        public val participantId: ParticipantId
            get() = throw NotImplementedError()

        /**
         * Requests an upgrade of the call to include media.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#calls).
         *
         * @param request a request body
         * @param token a valid token
         * @return an answer
         */
        public fun calls(request: CallsRequest, token: Token): Call<CallsResponse> =
            throw NotImplementedError()

        /**
         * Sends DTMF digits to the participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#dtmf).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> =
            throw NotImplementedError()

        /**
         * Requests to mute participant's audio.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#mute).
         *
         * @param token a valid token
         */
        public fun mute(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Requests to unmute participant's audio.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#mute).
         *
         * @param token a valid token
         */
        public fun unmute(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Signals that the participant has muted themselves.
         *
         * Only applicable to self.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#client_mute).
         *
         * @param token a valid token
         */
        public fun clientMute(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Signals that the participant has unmuted themselves.
         *
         * Only applicable to self.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#client_unmute).
         *
         * @param token a valid token
         */
        public fun clientUnmute(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Requests to mute participant's video.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#videomute).
         *
         * @param token a valid token
         */
        public fun videoMuted(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Requests to unmute participant's video.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#videomute).
         *
         * @param token a valid token
         */
        public fun videoUnmuted(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Requests to take presentation floor.
         *
         * @param token a valid token
         */
        public fun takeFloor(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Requests to release presentation floor.
         *
         * @param token a valid token
         */
        public fun releaseFloor(token: Token): Call<Unit> = throw NotImplementedError()

        /**
         * Sends a message to this participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#message).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun message(request: MessageRequest, token: Token): Call<Boolean> =
            throw NotImplementedError()

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
        ): Call<Boolean> = throw NotImplementedError()

        /**
         * Raises a participant's hand.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#buzz).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun buzz(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Lowers a participant's hand.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#clearbuzz).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun clearBuzz(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Enables the "spotlight" on a participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#spotlighton).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun spotlightOn(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Disables the "spotlight" on a participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#spotlighton).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun spotlightOff(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Lets a specified participant into the conference from the waiting room
         * of a locked conference.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#unlock_participant).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun unlock(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Disconnects a participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#disconnect).
         *
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun disconnect(token: Token): Call<Boolean> = throw NotImplementedError()

        /**
         * Changes the role of the participant.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#role).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if operation was successful, false otherwise
         */
        public fun role(request: RoleRequest, token: Token): Call<Boolean> =
            throw NotImplementedError()

        /**
         * Sets the call ID.
         *
         * @param callId an ID of the call
         */
        public fun call(callId: CallId): CallStep = throw NotImplementedError()
    }

    /**
     * Represents the [Call functions](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#call_functions) section.
     */
    public interface CallStep {

        /**
         * The participant step that produced this call step.
         */
        @Deprecated("Deprecated without a replacement.")
        public val participantStep: ParticipantStep
            get() = throw NotImplementedError()

        /**
         * A call ID that this call step will use.
         */
        public val callId: CallId
            get() = throw NotImplementedError()

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
        public fun newCandidate(request: NewCandidateRequest, token: Token): Call<Unit> =
            throw NotImplementedError()

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
        public fun ack(token: Token): Call<Unit> = throw NotImplementedError()

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
        public fun ack(request: AckRequest, token: Token): Call<Unit> = throw NotImplementedError()

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
        public fun update(request: UpdateRequest, token: Token): Call<UpdateResponse> =
            throw NotImplementedError()

        /**
         * Sends DTMF digits to the participant (gateway call only).
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm?Highlight=api#call_dtmf).
         *
         * @param request a request body
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> =
            throw NotImplementedError()

        /**
         * Disconnects the call.
         *
         * See [documentation](https://docs.pexip.com/api_client/api_rest.htm#call_disconnect).
         *
         * @param token a valid token
         * @return true if successful, false otherwise
         */
        public fun disconnect(token: Token): Call<Boolean> = throw NotImplementedError()
    }

    public companion object {

        internal val Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            serializersModule = SerializersModule {
                polymorphicDefaultDeserializer(InfinityEvent::class) {
                    UnknownEvent.serializer()
                }
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

        internal fun create(client: OkHttpClient, json: Json) = InfinityServiceImpl(
            client = client.newBuilder().readTimeout(1, TimeUnit.MINUTES).build(),
            json = json,
        )
    }
}
