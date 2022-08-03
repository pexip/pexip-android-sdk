package com.pexip.sdk.media.webrtc.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.properties.Delegates

internal class RealAudioHandler(private val context: Context, audioAttributes: AudioAttributes) :
    AudioHandler, AudioManager.OnAudioFocusChangeListener {

    private val handler = HandlerCompat.createAsync(Looper.getMainLooper())
    private val audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)!!
    private val audioFocusRequest =
        AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(AudioAttributesCompat.wrap(audioAttributes)!!)
            .setOnAudioFocusChangeListener(this)
            .build()
    private var snapshot: Snapshot? by Delegates.observable(null) { _, old, new ->
        if (old == null && new != null) {
            if (Build.VERSION.SDK_INT >= 28) {
                context.registerMicrophoneMuteReceiver()
            }
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isMicrophoneMute = false
            audioManager.requestAudioFocus(audioFocusRequest)
        }
        if (old != null && new == null) {
            audioManager.mode = old.mode
            audioManager.isMicrophoneMute = old.microphoneMute
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
            if (Build.VERSION.SDK_INT >= 28) {
                context.unregisterMicrophoneMuteReceiver()
            }
        }
    }
    private var startCount = 0
    private val microphoneMuteReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != AudioManager.ACTION_MICROPHONE_MUTE_CHANGED) return
            microphoneMuteListeners.notify()
        }
    }
    private val microphoneMuteListeners = CopyOnWriteArraySet<AudioHandler.MicrophoneMuteListener>()

    override var microphoneMute: Boolean
        get() = audioManager.isMicrophoneMute
        set(microphoneMute) {
            handler.post {
                if (snapshot != null) {
                    audioManager.isMicrophoneMute = microphoneMute
                    if (Build.VERSION.SDK_INT < 28) {
                        microphoneMuteListeners.notify()
                    }
                }
            }
        }

    override fun start() {
        handler.post {
            if (++startCount == 1) {
                snapshot = Snapshot(
                    mode = audioManager.mode,
                    microphoneMute = audioManager.isMicrophoneMute,
                )
            }
        }
    }

    override fun stop() {
        handler.post {
            if (--startCount == 0) {
                snapshot = null
            } else if (startCount < 0) {
                startCount = 0
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        // no op
    }

    override fun registerMicrophoneMuteListener(listener: AudioHandler.MicrophoneMuteListener) {
        handler.post {
            listener.onMicrophoneMute(audioManager.isMicrophoneMute)
        }
        microphoneMuteListeners += listener
    }

    override fun unregisterMicrophoneMuteListener(listener: AudioHandler.MicrophoneMuteListener) {
        microphoneMuteListeners -= listener
    }

    private fun AudioManager.requestAudioFocus(request: AudioFocusRequestCompat) {
        AudioManagerCompat.requestAudioFocus(this, request)
    }

    private fun AudioManager.abandonAudioFocusRequest(request: AudioFocusRequestCompat) {
        AudioManagerCompat.abandonAudioFocusRequest(this, request)
    }

    @RequiresApi(28)
    private fun Context.registerMicrophoneMuteReceiver() {
        val filter = IntentFilter(AudioManager.ACTION_MICROPHONE_MUTE_CHANGED)
        registerReceiver(microphoneMuteReceiver, filter, null, handler)
    }

    @RequiresApi(28)
    private fun Context.unregisterMicrophoneMuteReceiver() {
        try {
            unregisterReceiver(microphoneMuteReceiver)
        } catch (t: IllegalArgumentException) {
            // noop
        }
    }

    private fun Collection<AudioHandler.MicrophoneMuteListener>.notify() {
        val microphoneMute = audioManager.isMicrophoneMute
        forEach {
            it.onMicrophoneMute(microphoneMute)
        }
    }

    private data class Snapshot(val mode: Int, val microphoneMute: Boolean)
}
