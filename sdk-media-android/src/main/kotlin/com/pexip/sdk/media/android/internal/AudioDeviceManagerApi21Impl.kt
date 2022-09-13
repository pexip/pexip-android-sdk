package com.pexip.sdk.media.android.internal

import android.content.Context
import android.content.pm.PackageManager
import com.pexip.sdk.media.AudioDevice
import kotlin.properties.Delegates

internal class AudioDeviceManagerApi21Impl(context: Context) :
    AudioDeviceManagerBaseImpl<AudioDeviceApi21Impl>(context) {

    private val packageManager = context.packageManager
    private val hasTelephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    private val _availableAudioDevices = buildSortedMap {
        if (hasTelephony) {
            put(AudioDevice.Type.BUILTIN_SPEAKER)
            put(AudioDevice.Type.BUILTIN_EARPIECE)
        }
    }

    override var availableAudioDevices by Delegates.observable(availableAudioDevices()) { _, old, new ->
        if (old != new) onAvailableAudioDevicesChange(new)
    }
    override var selectedAudioDevice by Delegates.observable(selectedAudioDevice()) { _, old, new ->
        if (old != new) onSelectedAudioDeviceChange(new)
    }

    private val wiredHeadsetObserver = WiredHeadsetObserver(context, handler) { connected, name ->
        if (connected) {
            _availableAudioDevices.put(AudioDevice.Type.WIRED_HEADSET, name)
            if (hasTelephony) _availableAudioDevices.remove(AudioDevice.Type.BUILTIN_EARPIECE)
        } else {
            _availableAudioDevices.remove(AudioDevice.Type.WIRED_HEADSET)
            if (hasTelephony) _availableAudioDevices.put(AudioDevice.Type.BUILTIN_EARPIECE)
        }
        onAvailableAudioDevicesChange()
        onSelectedAudioDeviceChange()
    }
    private val bluetoothHeadsetObserver =
        BluetoothHeadsetObserver(context, handler) { connected, name ->
            if (connected) {
                _availableAudioDevices.put(AudioDevice.Type.BLUETOOTH_SCO, name)
            } else {
                _availableAudioDevices.remove(AudioDevice.Type.BLUETOOTH_SCO)
            }
            onAvailableAudioDevicesChange()
            onSelectedAudioDeviceChange()
        }
    private val bluetoothScoObserver = BluetoothScoObserver(context, handler) {
        onSelectedAudioDeviceChange()
    }

    override fun doSelectAudioDevice(audioDevice: AudioDeviceApi21Impl): Boolean {
        if (audioDevice == selectedAudioDevice) return false
        if (audioDevice !in _availableAudioDevices.values) return false
        return when (audioDevice.type) {
            AudioDevice.Type.WIRED_HEADSET -> {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                audioManager.isSpeakerphoneOn = false
                onSelectedAudioDeviceChange()
                true
            }
            AudioDevice.Type.BUILTIN_EARPIECE -> {
                if (AudioDevice.Type.WIRED_HEADSET in _availableAudioDevices) return false
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                audioManager.isSpeakerphoneOn = false
                onSelectedAudioDeviceChange()
                true
            }
            AudioDevice.Type.BUILTIN_SPEAKER -> {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                audioManager.isSpeakerphoneOn = true
                onSelectedAudioDeviceChange()
                true
            }
            AudioDevice.Type.BLUETOOTH_A2DP -> false
            AudioDevice.Type.BLUETOOTH_SCO -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = true
                audioManager.startBluetoothSco()
                // SCO state update will be broadcasted by the system later
                true
            }
        }
    }

    override fun doClearAudioDevice() {
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
        audioManager.isSpeakerphoneOn = false
    }

    override fun doDispose() {
        bluetoothScoObserver.close()
        bluetoothHeadsetObserver.close()
        wiredHeadsetObserver.close()
    }

    private fun onAvailableAudioDevicesChange() {
        availableAudioDevices = availableAudioDevices()
    }

    private fun onSelectedAudioDeviceChange() {
        selectedAudioDevice = selectedAudioDevice()
    }

    private fun availableAudioDevices() = _availableAudioDevices.values.toList()

    private fun selectedAudioDevice(): AudioDeviceApi21Impl? {
        val availableAudioDevices = _availableAudioDevices
        val builtinSpeaker = availableAudioDevices[AudioDevice.Type.BUILTIN_SPEAKER]
        if (builtinSpeaker != null && audioManager.isSpeakerphoneOn) return builtinSpeaker
        val bluetoothSco = availableAudioDevices[AudioDevice.Type.BLUETOOTH_SCO]
        if (bluetoothSco != null && bluetoothScoObserver.connected) return bluetoothSco
        val wiredHeadset = availableAudioDevices[AudioDevice.Type.WIRED_HEADSET]
        if (wiredHeadset != null) return wiredHeadset
        return availableAudioDevices[AudioDevice.Type.BUILTIN_EARPIECE]
    }

    private fun MutableMap<AudioDevice.Type, AudioDeviceApi21Impl>.put(
        type: AudioDevice.Type,
        name: String? = null,
    ) {
        this[type] = AudioDeviceApi21Impl(type, name)
    }
}
