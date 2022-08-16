package com.pexip.sdk.media.android

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjection
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnectionFactory

public interface MediaProjectionVideoTrackFactory {

    /**
     * Creates a [LocalVideoTrack] backed by [MediaProjection].
     *
     * The [intent] can be obtained after completing the [MediaProjection] permission request flow.
     *
     * The calling app must validate that the result code is [Activity.RESULT_OK] before calling
     * this method.
     *
     * ```
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
     *     val intent = manager.createScreenCaptureIntent()
     *     startActivityForResult(intent, REQUEST_CODE_MEDIA_PROJECTION)
     * }
     *
     * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     *     super.onActivityResult(requestCode, resultCode, data)
     *     if (requestCode != REQUEST_CODE_MEDIA_PROJECTION) return
     *     if (resultCode != Activity.RESULT_OK) return
     *     if (data == null) return
     *     val track = factory.createMediaProjectionVideoTrack(data, callback)
     * }
     * ```
     *
     * Starting with Android 10, you must also create a foreground service with `mediaProjection` type:
     *
     * ```
     * <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
     *
     * <application
     *     <service
     *         android:enabled="true"
     *         android:name=".MediaProjectionService"
     *         android:foregroundServiceType="mediaProjection" />
     * </application>
     * ```
     *
     * @param intent a data parameter obtained from [Activity.onActivityResult]
     * @param callback a callback to indicate when the session is no longer valid
     * @return a [MediaProjection]-backed [LocalVideoTrack]
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun createMediaProjectionVideoTrack(
        intent: Intent,
        callback: MediaProjection.Callback,
    ): LocalVideoTrack
}
