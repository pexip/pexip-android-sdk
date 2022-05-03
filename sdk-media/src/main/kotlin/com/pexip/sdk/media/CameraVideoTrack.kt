package com.pexip.sdk.media

public interface CameraVideoTrack : LocalVideoTrack {

    public fun switchCamera(callback: SwitchCameraCallback)

    public interface SwitchCameraCallback {

        public fun onSuccess(front: Boolean)

        public fun onFailure(error: String)
    }
}
