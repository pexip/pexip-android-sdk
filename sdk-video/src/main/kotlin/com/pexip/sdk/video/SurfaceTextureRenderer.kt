package com.pexip.sdk.video

import android.graphics.SurfaceTexture
import android.view.TextureView
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.ThreadUtils
import java.util.concurrent.CountDownLatch
import kotlin.properties.Delegates

/**
 * Display the video stream on a [SurfaceTexture].
 * This class is thread safe and handles access from potentially three different threads:
 * Interaction from the main app in init, release and setMirror.
 * Interaction from C++ rtc::VideoSinkInterface in renderFrame.
 */
public class SurfaceTextureRenderer(name: String) : TextureView.SurfaceTextureListener {

    private val renderer = EglRenderer(name)

    private var videoTrack by Delegates.observable<VideoTrack?>(null) { _, old, new ->
        ThreadUtils.checkIsOnMainThread()
        if (old == new) return@observable
        if (old != null) {
            old.value.removeSink(renderer)
            renderer.clearImage()
            renderer.release()
        }
        if (new != null) {
            renderer.init(new.context, EglBase.CONFIG_PLAIN, GlRectDrawer())
            new.value.addSink(renderer)
        }
    }

    public fun init(videoTrack: VideoTrack) {
        this.videoTrack = videoTrack
    }

    public fun release() {
        this.videoTrack = null
    }

    public fun setMirror(mirror: Boolean) {
        renderer.setMirror(mirror)
    }

    public fun setLayoutAspectRatio(ratio: Float) {
        renderer.setLayoutAspectRatio(ratio)
    }

    public fun setFpsReduction(fps: Float) {
        renderer.setFpsReduction(fps)
    }

    public fun disableFpsReduction() {
        renderer.disableFpsReduction()
    }

    public fun pauseVideo() {
        renderer.pauseVideo()
    }

    public fun clearImage() {
        renderer.clearImage()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        ThreadUtils.checkIsOnMainThread()
        renderer.createEglSurface(surface)
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        ThreadUtils.checkIsOnMainThread()
        val completionLatch = CountDownLatch(1)
        renderer.releaseEglSurface { completionLatch.countDown() }
        ThreadUtils.awaitUninterruptibly(completionLatch)
        return true
    }
}
