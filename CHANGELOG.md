# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.7.1] - 2022-08-04

### Added

- Infinity version info

### Fixed

- Unreliable capturing state notification for `LocalAudioTrack`

## [0.7.1] - 2022-08-02

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

[Unreleased]: https://github.com/pexip/pexip-android-sdk/compare/0.7.1...HEAD
[0.7.1]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.7.1
[0.7.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.7.0
[0.6.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.6.0
[0.5.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.5.0
[0.4.1]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.4.1
[0.4.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.4.0
[0.3.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.3.0
[0.2.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.2.0
[0.1.0]: https://github.com/pexip/pexip-android-sdk/releases/tag/0.1.0
