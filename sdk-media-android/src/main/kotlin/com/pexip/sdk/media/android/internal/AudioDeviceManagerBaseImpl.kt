package com.pexip.sdk.media.android.internal

import android.content.Context
import android.media.AudioManager
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.core.os.HandlerCompat
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.AudioDeviceManager
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class AudioDeviceManagerBaseImpl<T : AudioDevice>(context: Context) :
    AudioDeviceManager {

    protected val handler = HandlerCompat.createAsync(Looper.getMainLooper())
    protected val audioManager = context.getSystemService<AudioManager>()!!

    private val disposed = AtomicBoolean()
    private val initialMode = audioManager.mode
    private val audioAttributes = AudioAttributesCompat.Builder()
        .setContentType(AudioAttributesCompat.CONTENT_TYPE_SPEECH)
        .setUsage(AudioAttributesCompat.USAGE_VOICE_COMMUNICATION)
        .build()
    private val audioFocusRequest =
        AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener {
                // noop
            }
            .build()
    private val onSelectedAudioDeviceChangedListeners =
        CopyOnWriteArraySet<AudioDeviceManager.OnSelectedAudioDeviceChangedListener>()
    private val onAvailableAudioDevicesChangedListeners =
        CopyOnWriteArraySet<AudioDeviceManager.OnAvailableAudioDevicesChangedListener>()

    init {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        AudioManagerCompat.requestAudioFocus(audioManager, audioFocusRequest)
    }

    final override fun selectAudioDevice(audioDevice: AudioDevice): Boolean {
        if (disposed.get()) {
            throw IllegalStateException("${javaClass.name} has already been disposed!")
        }
        @Suppress("UNCHECKED_CAST")
        return doSelectAudioDevice(requireNotNull(audioDevice as? T))
    }

    final override fun clearAudioDevice() {
        if (disposed.get()) {
            throw IllegalStateException("${javaClass.name} has already been disposed!")
        }
        doClearAudioDevice()
    }

    final override fun registerOnSelectedAudioDeviceChanged(listener: AudioDeviceManager.OnSelectedAudioDeviceChangedListener) {
        onSelectedAudioDeviceChangedListeners += listener
    }

    final override fun unregisterOnSelectedAudioDeviceChanged(listener: AudioDeviceManager.OnSelectedAudioDeviceChangedListener) {
        onSelectedAudioDeviceChangedListeners -= listener
    }

    final override fun registerOnAvailableAudioDevicesChangedListener(listener: AudioDeviceManager.OnAvailableAudioDevicesChangedListener) {
        onAvailableAudioDevicesChangedListeners += listener
    }

    final override fun unregisterOnAvailableAudioDevicesChangedListener(listener: AudioDeviceManager.OnAvailableAudioDevicesChangedListener) {
        onAvailableAudioDevicesChangedListeners -= listener
    }

    final override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            try {
                doDispose()
                doClearAudioDevice()
            } finally {
                AudioManagerCompat.abandonAudioFocusRequest(audioManager, audioFocusRequest)
                audioManager.mode = initialMode
            }
        } else {
            throw IllegalStateException("${javaClass.name} has already been disposed!")
        }
    }

    protected fun onSelectedAudioDeviceChange(selectedAudioDevice: T?) {
        onSelectedAudioDeviceChangedListeners.forEach {
            it.onSelectedAudioDeviceChange(selectedAudioDevice)
        }
    }

    protected fun onAvailableAudioDevicesChange(availableAudioDevices: List<T>) {
        onAvailableAudioDevicesChangedListeners.forEach {
            it.onAvailableAudioDevicesChange(availableAudioDevices)
        }
    }

    protected abstract fun doSelectAudioDevice(audioDevice: T): Boolean

    protected abstract fun doClearAudioDevice()

    protected abstract fun doDispose()
}
