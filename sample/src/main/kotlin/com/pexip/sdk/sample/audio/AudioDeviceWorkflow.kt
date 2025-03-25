/*
 * Copyright 2022-2025 Pexip AS
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
package com.pexip.sdk.sample.audio

import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.AudioDeviceManager
import com.pexip.sdk.media.coroutines.getAvailableAudioDevices
import com.pexip.sdk.media.coroutines.getSelectedAudioDevice
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AudioDeviceWorkflow @Inject constructor(
    private val audioDeviceManager: Provider<AudioDeviceManager>,
) : StatefulWorkflow<AudioDeviceProps, AudioDeviceState, AudioDeviceOutput, AudioDeviceScreen>() {

    override fun initialState(props: AudioDeviceProps, snapshot: Snapshot?): AudioDeviceState =
        AudioDeviceState(audioDeviceManager.get())

    override fun snapshotState(state: AudioDeviceState): Snapshot? = null

    override fun render(
        renderProps: AudioDeviceProps,
        renderState: AudioDeviceState,
        context: RenderContext,
    ): AudioDeviceScreen {
        context.availableAudioDevicesSideEffect(renderState)
        context.selectedAudioDeviceSideEffect(renderState)
        context.disposeAudioDeviceManagerSideEffect(renderState)
        return AudioDeviceScreen(
            visible = renderProps.visible,
            availableAudioDevices = renderState.availableAudioDevices,
            selectedAudioDevice = renderState.selectedAudioDevice,
            onAudioDeviceClick = context.send(::onAudioDeviceClick),
            onBackClick = context.send(::onBackClick),
        )
    }

    private fun RenderContext.availableAudioDevicesSideEffect(renderState: AudioDeviceState) =
        runningSideEffect("availableAudioDevices(${renderState.audioDeviceManager})") {
            renderState.audioDeviceManager.getAvailableAudioDevices()
                .map(::onAvailableAudioDevicesChange)
                .collect(actionSink::send)
        }

    private fun RenderContext.selectedAudioDeviceSideEffect(renderState: AudioDeviceState) =
        runningSideEffect("selectedAudioDevice(${renderState.audioDeviceManager})") {
            renderState.audioDeviceManager.getSelectedAudioDevice()
                .map(::onSelectedAudioDeviceChange)
                .collect(actionSink::send)
        }

    private fun RenderContext.disposeAudioDeviceManagerSideEffect(renderState: AudioDeviceState) =
        runningSideEffect("disposeAudioDeviceManager(${renderState.audioDeviceManager})") {
            try {
                awaitCancellation()
            } finally {
                renderState.audioDeviceManager.dispose()
            }
        }

    private fun onAvailableAudioDevicesChange(availableAudioDevices: List<AudioDevice>) =
        action({ "onAvailableAudioDevicesChange($availableAudioDevices)" }) {
            state = state.copy(availableAudioDevices = availableAudioDevices)
        }

    private fun onSelectedAudioDeviceChange(selectedAudioDevice: AudioDevice?) =
        action({ "onSelectedAudioDeviceChange($selectedAudioDevice)" }) {
            state = state.copy(selectedAudioDevice = selectedAudioDevice)
        }

    private fun onAudioDeviceClick(audioDevice: AudioDevice) =
        action({ "onAudioDeviceClick($audioDevice" }) {
            state.audioDeviceManager.selectAudioDevice(audioDevice)
        }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(AudioDeviceOutput.Back)
    }
}
