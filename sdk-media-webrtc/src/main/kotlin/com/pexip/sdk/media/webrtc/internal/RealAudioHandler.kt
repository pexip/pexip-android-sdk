package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import kotlin.properties.Delegates

internal class RealAudioHandler(context: Context, audioAttributes: AudioAttributes) :
    AudioHandler, AudioManager.OnAudioFocusChangeListener {

    private val handler = HandlerCompat.createAsync(Looper.getMainLooper())
    private val audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)!!
    private val audioFocusRequest =
        AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(AudioAttributesCompat.wrap(audioAttributes)!!)
            .setOnAudioFocusChangeListener(this)
            .build()
    private var snapshot: Snapshot? by Delegates.observable(null) { _, old, new ->
        if (old != null) {
            AudioManagerCompat.abandonAudioFocusRequest(audioManager, audioFocusRequest)
            audioManager.mode = old.mode
            audioManager.isMicrophoneMute = old.microphoneMute
            audioManager.isSpeakerphoneOn = old.speakerphoneOn
        }
        if (new != null) {
            AudioManagerCompat.requestAudioFocus(audioManager, audioFocusRequest)
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isMicrophoneMute = false
            audioManager.isSpeakerphoneOn = false
        }
    }

    override fun start() {
        handler.post {
            snapshot = Snapshot(
                mode = audioManager.mode,
                microphoneMute = audioManager.isMicrophoneMute,
                speakerphoneOn = audioManager.isSpeakerphoneOn
            )
        }
    }

    override fun stop() {
        handler.post {
            snapshot = null
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        // no op
    }

    private data class Snapshot(
        val mode: Int,
        val microphoneMute: Boolean,
        val speakerphoneOn: Boolean,
    )
}
