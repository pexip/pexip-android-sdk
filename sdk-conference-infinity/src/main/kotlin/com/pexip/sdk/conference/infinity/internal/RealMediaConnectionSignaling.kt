/*
 * Copyright 2022 Pexip AS
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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.MediaConnectionSignaling
import kotlin.reflect.KProperty

internal class RealMediaConnectionSignaling(
    private val store: TokenStore,
    private val participantStep: InfinityService.ParticipantStep,
    override val iceServers: List<IceServer>,
) : MediaConnectionSignaling {

    var callStep: InfinityService.CallStep? by ThreadLocal()
    var pwds: Map<String, String>? by ThreadLocal()

    override fun onOffer(
        callType: String,
        description: String,
        presentationInMain: Boolean,
        fecc: Boolean,
    ): String {
        val token = store.get()
        pwds = getUfragPwd(description)
        return when (val step = callStep) {
            null -> {
                val request = CallsRequest(
                    callType = callType,
                    sdp = description,
                    present = if (presentationInMain) "main" else null,
                    fecc = fecc,
                )
                val response = participantStep.calls(request, token).execute()
                callStep = participantStep.call(response.callId)
                callStep!!.ack(token).execute()
                response.sdp
            }
            else -> {
                val request = UpdateRequest(
                    sdp = description,
                    fecc = fecc,
                )
                val response = step.update(request, token).execute()
                response.sdp
            }
        }
    }

    override fun onCandidate(candidate: String, mid: String) {
        val callStep = checkNotNull(callStep) { "callStep is not set." }
        val pwds = checkNotNull(pwds) { "pwds are not set." }
        val token = store.get()
        val ufrag = getUfrag(candidate)
        val request = NewCandidateRequest(
            candidate = candidate,
            mid = mid,
            ufrag = ufrag,
            pwd = pwds[ufrag],
        )
        callStep.newCandidate(request, token).execute()
    }

    override fun onDtmf(digits: String) {
        val callStep = checkNotNull(callStep) { "callStep is not set." }
        val request = DtmfRequest(digits)
        val token = store.get()
        callStep.dtmf(request, token).execute()
    }

    override fun onAudioMuted() {
        participantStep.mute(store.get()).execute()
    }

    override fun onAudioUnmuted() {
        participantStep.unmute(store.get()).execute()
    }

    override fun onVideoMuted() {
        participantStep.videoMuted(store.get()).execute()
    }

    override fun onVideoUnmuted() {
        participantStep.videoUnmuted(store.get()).execute()
    }

    override fun onTakeFloor() {
        participantStep.takeFloor(store.get()).execute()
    }

    override fun onReleaseFloor() {
        participantStep.releaseFloor(store.get()).execute()
    }

    private fun getUfrag(candidate: String) =
        checkNotNull(CANDIDATE_UFRAG.matchEntire(candidate)?.groupValues?.get(1))

    private fun getUfragPwd(description: String) = buildMap {
        val iterator = description.splitToSequence("\r\n").iterator()
        while (iterator.hasNext()) {
            var line = iterator.next()
            val ufrag = SDP_UFRAG.matchEntire(line)?.groupValues?.get(1) ?: continue
            line = iterator.next()
            val pwd = SDP_PWD.matchEntire(line)?.groupValues?.get(1) ?: continue
            put(ufrag, pwd)
        }
    }

    companion object {

        val SDP_UFRAG = Regex("^a=ice-ufrag:(.+)$")
        val SDP_PWD = Regex("^a=ice-pwd:(.+)$")
        val CANDIDATE_UFRAG = Regex(".*\\bufrag\\s+(.+?)\\s+.*")
    }
}

private operator fun <T> ThreadLocal<T>.getValue(thisRef: Any?, property: KProperty<*>) = get()

private operator fun <T> ThreadLocal<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) =
    set(value)