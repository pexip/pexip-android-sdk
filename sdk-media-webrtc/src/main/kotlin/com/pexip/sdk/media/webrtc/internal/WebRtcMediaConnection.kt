package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.Bitrate
import com.pexip.sdk.media.Bitrate.Companion.bps
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack.MediaType
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

internal class WebRtcMediaConnection(
    factory: WebRtcMediaConnectionFactory,
    private val config: MediaConnectionConfig,
    private val workerExecutor: Executor,
    private val networkExecutor: Executor,
    private val signalingExecutor: Executor,
) : MediaConnection, SimplePeerConnectionObserver {

    private val started = AtomicBoolean()
    private val disposed = AtomicBoolean()
    private val shouldRenegotiate = AtomicBoolean()
    private val connection = factory.createPeerConnection(createRTCConfiguration(), this)

    private var bitrate by Delegates.observable(0.bps) { _, old, new ->
        if (old != new) connection.restartIce()
    }

    // Rename due to platform declaration clash
    @set:JvmName("setMainAudioTrackInternal")
    private var mainAudioTrack: LocalAudioTrack? by Delegates.observable(null) { _, old, new ->
        if (started.get() && old != new && new != null) onMainAudioCapturingChange(new.capturing)
        old?.unregisterCapturingListener(mainAudioTrackCapturingListener)
        new?.registerCapturingListener(mainAudioTrackCapturingListener)
    }

    // Rename due to platform declaration clash
    @set:JvmName("setMainVideoTrackInternal")
    private var mainVideoTrack: LocalVideoTrack? by Delegates.observable(null) { _, old, new ->
        if (started.get() && old != new && new != null) onMainVideoCapturingChange(new.capturing)
        old?.unregisterCapturingListener(mainVideoTrackCapturingListener)
        new?.registerCapturingListener(mainVideoTrackCapturingListener)
    }

    // Rename due to platform declaration clash
    @set:JvmName("setPresentationVideoTrackInternal")
    private var presentationVideoTrack: LocalVideoTrack? by Delegates.observable(null) { _, old, new ->
        if (old == new) return@observable
        if (new != null) onTakeFloor() else onReleaseFloor()
    }

    private var mainAudioTransceiver: RtpTransceiver? = null
    private var mainVideoTransceiver: RtpTransceiver? = null
    private val presentationVideoTransceiver: RtpTransceiver = connection.addTransceiver(
        MediaType.MEDIA_TYPE_VIDEO,
        RtpTransceiver.RtpTransceiverInit(RtpTransceiverDirection.INACTIVE)
    )
    private val mainAudioTransceiverLock = Any()
    private val mainVideoTransceiverLock = Any()
    private val localSdpObserver = object : SimpleSdpObserver {

        override fun onCreateSuccess(description: SessionDescription) {
            setLocalDescription(this, description)
        }

        override fun onSetSuccess() {
            onSetLocalDescriptionSuccess()
        }
    }
    private val remoteSdpObserver = object : SimpleSdpObserver {

        override fun onSetSuccess() {
            mainAudioTrack?.let { onMainAudioCapturingChange(it.capturing) }
            mainVideoTrack?.let { onMainVideoCapturingChange(it.capturing) }
        }
    }
    private val mainRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val presentationRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val mainAudioTrackCapturingListener = LocalMediaTrack.CapturingListener {
        if (started.get()) onMainAudioCapturingChange(it)
    }
    private val mainVideoTrackCapturingListener = LocalMediaTrack.CapturingListener {
        if (started.get()) onMainVideoCapturingChange(it)
    }

    override val mainRemoteVideoTrack: com.pexip.sdk.media.VideoTrack?
        get() = synchronized(mainVideoTransceiverLock) {
            mainVideoTransceiver?.takeUnless { disposed.get() }
                ?.takeIf { it.direction == RtpTransceiverDirection.SEND_RECV }
                ?.receiver
                ?.videoTrack
                ?.let(::WebRtcVideoTrack)
        }

    override val presentationRemoteVideoTrack: com.pexip.sdk.media.VideoTrack?
        get() = synchronized(presentationVideoTransceiver) {
            presentationVideoTransceiver.takeUnless { disposed.get() }
                ?.takeIf { it.direction == RtpTransceiverDirection.RECV_ONLY || it.direction == RtpTransceiverDirection.SEND_RECV }
                ?.receiver
                ?.videoTrack
                ?.let(::WebRtcVideoTrack)
        }

    override fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?) {
        val lat = when (localAudioTrack) {
            is WebRtcLocalAudioTrack -> localAudioTrack
            null -> null
            else -> throw IllegalArgumentException("localAudioTrack must be null or an instance of WebRtcLocalAudioTrack.")
        }
        workerExecutor.maybeExecuteUnlessDisposed {
            synchronized(mainAudioTransceiverLock) {
                val t = mainAudioTransceiver ?: connection.maybeAddTransceiver(lat)
                t?.maybeSetNewDirection(lat)
                t?.setTrack(lat)
                mainAudioTransceiver = t
            }
            mainAudioTrack = lat
        }
    }

    override fun setMainVideoTrack(localVideoTrack: LocalVideoTrack?) {
        val lvt = when (localVideoTrack) {
            is WebRtcLocalVideoTrack -> localVideoTrack
            null -> null
            else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
        }
        workerExecutor.maybeExecuteUnlessDisposed {
            synchronized(mainVideoTransceiverLock) {
                val t = mainVideoTransceiver ?: connection.maybeAddTransceiver(lvt)
                t?.maybeSetNewDirection(lvt)
                t?.setTrack(lvt)
                mainVideoTransceiver = t
            }
            mainVideoTrack = lvt
        }
    }

    override fun setPresentationVideoTrack(localVideoTrack: LocalVideoTrack?) {
        val lvt = when (localVideoTrack) {
            is WebRtcLocalVideoTrack -> localVideoTrack
            null -> null
            else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
        }
        workerExecutor.maybeExecuteUnlessDisposed {
            synchronized(presentationVideoTransceiver) {
                presentationVideoTransceiver.maybeSetNewDirection(lvt)
                presentationVideoTransceiver.setTrack(lvt)
            }
            presentationVideoTrack = lvt
        }
    }

    override fun setMainRemoteAudioTrackEnabled(enabled: Boolean) {
        workerExecutor.maybeExecuteUnlessDisposed {
            mainAudioTransceiver = mainAudioTransceiver ?: connection.maybeAddTransceiver(
                mediaType = MediaType.MEDIA_TYPE_AUDIO,
                receive = enabled
            )
            mainAudioTransceiver?.maybeSetNewDirection(enabled)
        }
    }

    override fun setMainRemoteVideoTrackEnabled(enabled: Boolean) {
        workerExecutor.maybeExecuteUnlessDisposed {
            mainVideoTransceiver = mainVideoTransceiver ?: connection.maybeAddTransceiver(
                mediaType = MediaType.MEDIA_TYPE_VIDEO,
                receive = enabled
            )
            mainVideoTransceiver?.maybeSetNewDirection(enabled)
        }
    }

    override fun setPresentationRemoteVideoTrackEnabled(enabled: Boolean) {
        if (!config.presentationInMain) workerExecutor.maybeExecuteUnlessDisposed {
            synchronized(presentationVideoTransceiver) {
                presentationVideoTransceiver.maybeSetNewDirection(enabled)
            }
        }
    }

    override fun setMaxBitrate(bitrate: Bitrate) {
        workerExecutor.maybeExecuteUnlessDisposed {
            this.bitrate = bitrate
        }
    }

    @Deprecated(
        message = "Use setPresentationVideoReceive(true) instead.",
        replaceWith = ReplaceWith("setPresentationVideoReceive(true)")
    )
    override fun startPresentationReceive() = setPresentationRemoteVideoTrackEnabled(true)

    @Deprecated(
        message = "Use setPresentationVideoReceive(false) instead.",
        replaceWith = ReplaceWith("setPresentationVideoReceive(false)")
    )
    override fun stopPresentationReceive() = setPresentationRemoteVideoTrackEnabled(false)

    override fun dtmf(digits: String) {
        networkExecutor.maybeExecuteUnlessDisposed {
            runCatching { config.signaling.onDtmf(digits) }
        }
    }

    override fun start() {
        if (started.compareAndSet(false, true)) {
            createOffer()
        }
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            workerExecutor.execute {
                mainAudioTrack = null
                mainVideoTrack = null
                presentationVideoTrack = null
                synchronized(mainAudioTransceiverLock) {
                    mainAudioTransceiver?.sender?.setTrack(null, false)
                }
                synchronized(mainVideoTransceiverLock) {
                    mainVideoTransceiver?.sender?.setTrack(null, false)
                }
                synchronized(presentationVideoTransceiver) {
                    presentationVideoTransceiver.sender.setTrack(null, false)
                }
                mainRemoteVideoTrackListeners.clear()
                presentationRemoteVideoTrackListeners.clear()
                connection.dispose()
            }
        }
    }

    override fun registerMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        mainRemoteVideoTrackListeners += listener
    }

    override fun unregisterMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        mainRemoteVideoTrackListeners -= listener
    }

    override fun registerPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        presentationRemoteVideoTrackListeners += listener
    }

    override fun unregisterPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        presentationRemoteVideoTrackListeners -= listener
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        onCandidate(candidate.sdp, candidate.sdpMid)
    }

    override fun onRenegotiationNeeded() {
        // Skip the first call to onRenegotiationNeeded() since it's called right after
        // PeerConnection creation and we're still not ready to use createOffer()
        if (shouldRenegotiate.compareAndSet(false, true)) return
        createOffer()
    }

    override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
        val videoTrack = receiver.videoTrack?.let(::WebRtcVideoTrack)
        when (receiver.id()) {
            synchronized(mainVideoTransceiverLock) { mainVideoTransceiver?.receiver?.id() } -> {
                mainRemoteVideoTrackListeners.notify(videoTrack)
            }
            synchronized(presentationVideoTransceiver) { presentationVideoTransceiver.receiver.id() } -> {
                presentationRemoteVideoTrackListeners.notify(videoTrack)
            }
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        when (receiver.id()) {
            synchronized(mainVideoTransceiverLock) { mainVideoTransceiver?.receiver?.id() } -> {
                mainRemoteVideoTrackListeners.notify(null)
            }
            synchronized(presentationVideoTransceiver) { presentationVideoTransceiver.receiver.id() } -> {
                presentationRemoteVideoTrackListeners.notify(null)
            }
        }
    }

    private fun createOffer() {
        workerExecutor.maybeExecuteUnlessDisposed {
            connection.createOffer(localSdpObserver, MediaConstraints())
        }
    }

    private fun setLocalDescription(observer: SdpObserver, description: SessionDescription) {
        workerExecutor.maybeExecuteUnlessDisposed {
            connection.setLocalDescription(observer, description)
        }
    }

    private fun setRemoteDescription(sdp: SessionDescription) {
        workerExecutor.maybeExecuteUnlessDisposed {
            connection.setRemoteDescription(remoteSdpObserver, sdp.mangle(bitrate))
        }
    }

    private fun onSetLocalDescriptionSuccess() {
        workerExecutor.maybeExecuteUnlessDisposed {
            val mangledDescription = connection.localDescription.mangle(
                bitrate = bitrate,
                mainAudioMid = synchronized(mainAudioTransceiverLock) {
                    mainAudioTransceiver?.mid
                },
                mainVideoMid = synchronized(mainVideoTransceiverLock) {
                    mainVideoTransceiver?.mid
                },
                presentationVideoMid = synchronized(presentationVideoTransceiver) {
                    presentationVideoTransceiver.mid
                }
            )
            networkExecutor.maybeExecute {
                try {
                    val sdp = SessionDescription(
                        SessionDescription.Type.ANSWER,
                        config.signaling.onOffer(
                            callType = "WEBRTC",
                            description = mangledDescription.description,
                            presentationInMain = config.presentationInMain,
                            fecc = config.farEndCameraControl
                        )
                    )
                    setRemoteDescription(sdp)
                } catch (t: Throwable) {
                    // noop
                }
            }
        }
    }

    private fun onCandidate(candidate: String, mid: String) {
        networkExecutor.maybeExecute {
            runCatching { config.signaling.onCandidate(candidate, mid) }
        }
    }

    private fun onMainAudioCapturingChange(capturing: Boolean) {
        networkExecutor.maybeExecute {
            val f = when (capturing) {
                true -> config.signaling::onAudioUnmuted
                else -> config.signaling::onAudioMuted
            }
            runCatching(f)
        }
    }

    private fun onMainVideoCapturingChange(capturing: Boolean) {
        networkExecutor.maybeExecute {
            val f = when (capturing) {
                true -> config.signaling::onVideoUnmuted
                else -> config.signaling::onVideoMuted
            }
            runCatching(f)
        }
    }

    private fun Collection<MediaConnection.RemoteVideoTrackListener>.notify(videoTrack: WebRtcVideoTrack?) {
        signalingExecutor.maybeExecute {
            forEach {
                it.safeOnRemoteVideoTrack(videoTrack)
            }
        }
    }

    private fun onTakeFloor() {
        networkExecutor.maybeExecute {
            runCatching(config.signaling::onTakeFloor)
        }
    }

    private fun onReleaseFloor() {
        networkExecutor.maybeExecute {
            runCatching(config.signaling::onReleaseFloor)
        }
    }

    private fun createRTCConfiguration(): PeerConnection.RTCConfiguration {
        val iceServers = config.iceServers.map {
            PeerConnection.IceServer.builder(it.urls.toList())
                .setUsername(it.username)
                .setPassword(it.password)
                .createIceServer()
        }
        val c = PeerConnection.RTCConfiguration(iceServers)
        c.enableDscp = config.dscp
        c.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        c.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        return c
    }

    private val RtpReceiver.videoTrack
        get() = track() as? org.webrtc.VideoTrack

    private inline fun Executor.maybeExecuteUnlessDisposed(crossinline block: () -> Unit) {
        if (!disposed.get()) maybeExecute {
            if (!disposed.get()) block()
        }
    }
}
