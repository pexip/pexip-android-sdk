/*
 * Copyright 2023 Pexip AS
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
package com.pexip.sdk.media.webrtc.internal

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import assertk.assertions.prop
import org.webrtc.DtmfSender
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpSender
import org.webrtc.RtpTransceiver
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Member
import kotlin.test.Test

internal class ReflectionTest {

    @Test
    fun `nativeGetTransceivers is accessible`() {
        assertThat(NativeGetTransceivers).all {
            hasDeclaringClass<PeerConnection>()
            hasName("nativeGetTransceivers")
            isAccessible()
        }
    }

    @Test
    fun `transceivers is accessible`() {
        assertThat(Transceivers).all {
            hasDeclaringClass<PeerConnection>()
            hasName("transceivers")
            isAccessible()
        }
    }

    @Test
    fun `nativeDtmfSender is accessible`() {
        assertThat(NativeDtmfSender).all {
            hasDeclaringClass<DtmfSender>()
            hasName("nativeDtmfSender")
            isAccessible()
        }
    }

    @Test
    fun `nativeTrack is accessible`() {
        assertThat(NativeTrack).all {
            hasDeclaringClass<MediaStreamTrack>()
            hasName("nativeTrack")
            isAccessible()
        }
    }

    @Test
    fun `ownsTrack is accessible`() {
        assertThat(OwnsTrack).all {
            hasDeclaringClass<RtpSender>()
            hasName("ownsTrack")
            isAccessible()
        }
    }

    @Test
    fun `nativeRtpSender is accessible`() {
        assertThat(NativeRtpSender).all {
            hasDeclaringClass<RtpSender>()
            hasName("nativeRtpSender")
            isAccessible()
        }
    }

    @Test
    fun `nativeObserver is accessible`() {
        assertThat(NativeObserver).all {
            hasDeclaringClass<RtpReceiver>()
            hasName("nativeObserver")
            isAccessible()
        }
    }

    @Test
    fun `nativeUnsetObserver is accessible`() {
        assertThat(NativeUnsetObserver).all {
            hasDeclaringClass<RtpReceiver>()
            hasName("nativeUnsetObserver")
            isAccessible()
        }
    }

    @Test
    fun `nativeRtpReceiver is accessible`() {
        assertThat(NativeRtpReceiver).all {
            hasDeclaringClass<RtpReceiver>()
            hasName("nativeRtpReceiver")
            isAccessible()
        }
    }

    @Test
    fun `nativeRtpTransceiver is accessible`() {
        assertThat(NativeRtpTransceiver).all {
            hasDeclaringClass<RtpTransceiver>()
            hasName("nativeRtpTransceiver")
            isAccessible()
        }
    }

    private inline fun <reified T : Any> Assert<Member>.hasDeclaringClass() =
        prop("declaringclass", Member::getDeclaringClass).isEqualTo(T::class.java)

    private fun Assert<Member>.hasName(expected: String) =
        prop("name", Member::getName).isEqualTo(expected)

    private fun Assert<AccessibleObject>.isAccessible() =
        prop("isAccessible", AccessibleObject::isAccessible).isTrue()
}
