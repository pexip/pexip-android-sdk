package com.pexip.sdk.sample.audio

import com.pexip.sdk.media.AudioDeviceManager
import com.pexip.sdk.media.coroutines.getAvailableAudioDevices
import com.pexip.sdk.media.coroutines.getSelectedAudioDevice
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AudioDeviceWorkflow @Inject constructor(private val audioDeviceManager: Provider<AudioDeviceManager>) :
    StatefulWorkflow<AudioDeviceProps, AudioDeviceState, AudioDeviceOutput, AudioDeviceRendering>() {

    override fun initialState(props: AudioDeviceProps, snapshot: Snapshot?): AudioDeviceState =
        AudioDeviceState(audioDeviceManager.get())

    override fun snapshotState(state: AudioDeviceState): Snapshot? = null

    override fun render(
        renderProps: AudioDeviceProps,
        renderState: AudioDeviceState,
        context: RenderContext,
    ): AudioDeviceRendering {
        context.availableAudioDevicesSideEffect(renderState)
        context.selectedAudioDeviceSideEffect(renderState)
        context.disposeAudioDeviceManagerSideEffect(renderState)
        return AudioDeviceRendering(
            visible = renderProps.visible,
            availableAudioDevices = renderState.availableAudioDevices,
            selectedAudioDevice = renderState.selectedAudioDevice,
            onAudioDeviceClick = context.send(::OnAudioDeviceClick),
            onBackClick = context.send(::OnBackClick)
        )
    }

    private fun RenderContext.availableAudioDevicesSideEffect(renderState: AudioDeviceState) =
        runningSideEffect("availableAudioDevices(${renderState.audioDeviceManager})") {
            renderState.audioDeviceManager.getAvailableAudioDevices()
                .map(::OnAvailableAudioDevicesChange)
                .collect(actionSink::send)
        }

    private fun RenderContext.selectedAudioDeviceSideEffect(renderState: AudioDeviceState) =
        runningSideEffect("selectedAudioDevice(${renderState.audioDeviceManager})") {
            renderState.audioDeviceManager.getSelectedAudioDevice()
                .map(::OnSelectedAudioDeviceChange)
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
}
