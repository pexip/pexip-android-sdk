# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `Conference.name` property
- `WebRtcMediaConnectionFactory.createLocalVideoTrack` that accepts `VideoCapturer`
- `ParticipantStep.message` that allows sending direct messages
- `MessageReceivedEvent.direct` that indicates whether it's direct message

### Changed

- WebRTC to M110
- **BREAKING**: `MessageRequest` no longer has a default `type` set

## [0.11.0] - 2022-12-15

### Added

- `WebRtcMediaConnectionFactory.Builder` to improve experience for both Java and Kotlin users
- `MediaConnection.setPresentationRemoteVideoTrackEnabled` that allows to configure whether
  the `MediaConnection` should be receiving remote presentation
- `MediaConnection.setMainRemoteAudioTrackEnabled`/`MediaConnection.setMainRemoteVideoTrackEnabled`
  to control whether the main remote audio/video should be enabled
- `MediaConnection.setMaxBitrate` that controls maximum bitrate for each video stream
- Several new properties on `RequestTokenResponse` and `Registration`
- Ability to retrieve registered devices via `Registration.getRegisteredDevices`

### Changed

- Kotlin to 1.7.20
- `EglBase` is now nullable
- Deprecate `WebRtcMediaConnectionFactory` constructor, please migrate to `Builder`
- **BREAKING**: `MediaConnection.setMainAudioTrack`/`MediaConnection.setMainVideoTrack` no longer
  enable remote audio/video by default. Please use `MediaConnection.setMainRemoteAudioTrackEnabled`
  /`MediaConnection.setMainRemoteVideoTrackEnabled` to enable them

### Fixed

- Microphone mute state not being restored after `LocalAudioTrack.dispose` call

## [0.10.0] - 2022-09-23

### Added

- `AudioDeviceManager` that provides available audio devices, selected audio device and allows you
  to switch to a different one. Implementations of this interface will also manage audio focus
  request in place of `LocalAudioTrack`
- Support for `zOrderMediaOverlay` and `zOrderOnTop` in `VideoTrackRenderer`
- Methods to list available device names (`getDeviceNames()`), available quality profiles for a
  specific device (`getQualityProfiles(String)`) and whether the device is
  front-facing (`isFrontFacing(String)`) or back-facing (`isBackFacing(String)`)
- `CameraVideoTrack.switchCamera(String, CameraVideoTrack.SwitchCameraCallback)` that allows you to
  switch to a specific device
- Suspending `CameraVideoTrack.switchCamera(String?)`
- `NodeResolver.create(ResolverApi)` that allows to pass a custom `ResolverApi` instance
- `MediaConnection.mainRemoteVideoTrack` and `MediaConnection.presentationRemoteVideoTrack`
  properties
- `MediaConnection.mainRemoteVideoTrackIn(CoroutineScope, SharingStarted)`
  and `MediaConnection.mainRemoteVideoTrackIn(CoroutineScope, SharingStarted)`
- `LocalMediaTrack.capturing` property
- `LocalMediaTrack.captureIn(CoroutineScope, SharingStarted)`

### Changed

- **BREAKING**: Various listeners will no longer be notified upon registration. Components that
  followed this pattern now provide respective properties that let you read the current
  value. `MediaConnection`, `LocalMediaTrack` were affected by this change
- `CameraVideoTrack.switchCamera()` docs to mention that it will attempt to switch to the next
  device name in the available devices list, rather than to opposite-facing device
- `CameraVideoTrack.SwitchCameraCallback.onSuccess(String)` to return device name instead of whether
  the camera is front-facing; `onSuccess(Boolean)` is deprecated and will still be called until it
  is removed
- Various deprecated methods with `DeprecationLevel.ERROR` were removed
- Various deprecated methods `DeprecationLevel` to `DeprecationLevel.ERROR`

## [0.9.0] - 2022-08-25

### Added

- `CallStep.dtmf()`
- `MediaConnection.dtmf()` as a replacement for `Conference.dtmf()`, best suited for gateway calls

## [0.8.0] - 2022-08-16

### Added

- `DisconnectedConferenceEvent`, `FailureConferenceEvent` and `FailureRegistrationEvent`
- `RegistrationEvent.at` property
- `LocalEglBase` in `sdk-media-webrtc-compose` that must be set before using `VideoTrackRenderer`

### Changed

- `compileSdk` to 32
- `androidx.compose` to 1.2.0
- Notify various `MediaConnection` listeners on main thread
- Break down `MediaConnectionFactory` into various factories for tracks

## [0.7.1] - 2022-08-04

### Added

- Infinity version info

### Fixed

- Unreliable capturing state notification for `LocalAudioTrack`

## [0.7.0] - 2022-08-02

### Added

- `sdk-registration-*` to support registration with Infinity
- `CameraVideoTrack.Callback` to provide notifications when the camera is disconnected
- `MediaConnectionFactory.createCameraVideoTrack()` that accepts `CameraVideoTrack.Callback`

### Changed

- Bumped Kotlin to 1.6.21
- Capturing state for `LocalAudioTrack` is now backed by `AudioManager.getMicrophoneMute()`
- Deprecated `MediaConnectionFactory.createCameraVideoTrack()` that doesn't
  accept `CameraVideoTrack.Callback`

## [0.6.0] - 2022-06-30

### Added

- An option to enable DSCP (see `MediaConnectionConfig`)
- Ability to set presentation video to send to the conference

### Changed

- Threading in `MediaConnection`
- Deprecated `MediaConnection.sendMainAudio/sendMainVideo` in favor
  of `setMainAudioTrack/setMainVideoTrack`
- Various deprecation levels to `ERROR`

## [0.5.0] - 2022-06-16

### Added

- Basic module-level documentation
- `takeFloor`/`releaseFloor` REST API methods
- `AndroidMediaConnectionFactory.createMediaProjectionVideoTrack` used to capture screen content

### Changed

- `InfinityService.ConferenceStep.requestToken` to accept an empty string (with the same behavior
  as `"none"`)
- **BREAKING**: `MediaConnectionFactory.createCameraVideoTrack` will now
  throw `IllegalStateException` if there are no available cameras or a camera with `deviceName` does
  not exist

## [0.4.1] - 2022-06-09

### Added

- [API reference](https://pexip.github.io/pexip-android-sdk/)

## [0.4.0] - 2022-06-09

### Added

- `Renderer` marker interface to denote objects that can render an instance of a `VideoTrack`
- **BREAKING**: `SurfaceViewRenderer` as a replacement for `org.webrtc.SurfaceViewRenderer`
- Custom `org.webrtc.CameraEnumerator` support in `WebRtcMediaConnectionFactory`

### Changed

- **BREAKING**: Library type from Android to JVM for most of the artifacts

## [0.3.0] - 2022-06-03

### Added

- DTMF support
- SDK will now correctly set `AudioManager` mode and request audio focus

## [0.2.0] - 2022-05-27

### Added

- Proguard rules to `sdk-api-infinity`
- Version catalog artifact
- STUN/TURN parsing from Infinity node
- `analytics_enabled` field

## [0.1.0] - 2022-05-19

### Added

- Initial release

[Unreleased]: https://github.com/pexip/pexip-android-sdk/compare/0.11.0...HEAD
[0.11.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.11.0
[0.10.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.10.0
[0.9.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.9.0
[0.8.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.8.0
[0.7.1]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.7.1
[0.7.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.7.0
[0.6.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.6.0
[0.5.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.5.0
[0.4.1]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.4.1
[0.4.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.4.0
[0.3.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.3.0
[0.2.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.2.0
[0.1.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.1.0
