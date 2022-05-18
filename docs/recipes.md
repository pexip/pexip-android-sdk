# Recipes

## Resolving a node

First, make sure to initialize the `NodeResolver` (you need to do this just once):

```kotlin
NodeResolver.initialize(context)
```

Create an instance of `NodeResolver` (it can be a singleton instance) and use it to resolve node
addresses for a `host`:

```kotlin
val uri = "george@example.com"
val (_, host) = uri.split("@")

val nodeResolver = NodeResolver.create(dnssec = false)
val nodes: List<URL> = nodeResolver.resolve(host).execute()
```

Next, you must pick a node addresses that is available. This can be achieved via `InfinityService`:

```kotlin
// You should generally provide your own instance of OkHttpClient to share its thread pool
val okHttpClient = OkHttpClient()
val infinityService = InfinityService.create(okHttpClient)
val node: URL? = nodees.find { infinityService.newRequest(it).status().execute() }
```

## Joining a conference

To interact with an Infinity conference, you must have a node address, a conference alias and a
token. To join a conference, you should do the following:

```kotlin
val node = URL("example.com")
val conferenceAlias = "george"
val request = RequestTokenRequest(displayName = "John")
val response = infinityService.newRequest(node)
    .conference(conferenceAlias)
    .requestToken(request)
    .execute()
```

There are a number of cases when this method can throw - mainly when some sort of authentication is
required (e.g. `RequiredPinException` when either a host or a guest PIN is required). These cases
must be handled properly.

## Interacting with a conference

Once you get a valid `RequestTokenResponse`, you should be able to create an instance of
the `Conference`.

```kotlin
val conference = Conference.create(
    service = infinityService,
    node = node,
    conferenceAlias = conferenceAlias,
    response = response
)
val listener = ConferenceEventListener { conferenceEvent ->
    // Do something fascinating with a conferenceEvent
}
conference.registerConferenceEventListener(listener)
// Send a chat message
conference.message("Hi!")
// Don't forget to unregister your listener once done with the Conference
conference.unregisterConferenceEventListener(listener)
conference.leave()
```

It's advisable to create keep this object in a component that survives configuration changes, such
as AndroidX's `ViewModel`.

## Accessing camera and microphone

To access your camera and microphone, you need to create a `MediaConnectionFactory` instance
(node that currently, only `WebRtcMediaConnectionFactory` is available). Make sure to initialize it
once:

```kotlin
WebRtcMediaConnectionFactory.initialize(context)
```

After that, ensure that `android.permission.RECORD_AUDIO` and `android.permission.CAMERA` were
granted to get access to microphone and camera respectively.

Now, use provided methods to create instances of `LocalMediaTrack`:

```kotlin
val localAudioTrack: LocalAudioTrack = factory.createLocalAudioTrack()
val cameraVideoTrack: CameraVideoTrack = factory.createCameraVideoTrack()
localAudioTrack.startCapture()
cameraVideoTrack.startCapture(QualityProfile.High)
// Once done with the tracks, make sure to call dispose()
```

## Joining the conference with audio and video

To join the conference with audio and video, you need an instance of a `Conference`. Note that you
must provide an instance of `EglBase` to be able to utilize hardware decoding/encoding and render
the video:

```kotlin
val eglBase = EglBase.create()
val factory: MediaConnectionFactory = WebRtcMediaConnectionFactory(
    context = context,
    eglBase = eglBase
)
val iceServer = IceServer.Builder(listOf("stun:example.com:19302")).build()
val config = MediaConnectionConfig.Builder(conference)
    .addIceServer(iceServer) // Optional
    .presentationInMain(false)
    .build()
val mediaConnection: MediaConnection = factory.createMediaConnection(config)
mediaConnection.sendMainAudio(localAudioTrack)
mediaConnection.sendMainVideo(cameraVideoTrack)
val mainRemoteVideoTrackListener = RemoteVideoTrackListener { videoTrack ->
    // Render onto a surface
}
mediaConnection.registerMainRemoteVideoTrackListener(mainRemoteVideoTrackListener)
mediaConnection.start()
// And don't forget to dispose it once finished
mediaConnection.dispose()
```

## Rendering the video

To render a `VideoTrack`, call `addRenderer` with a `SurfaceViewRenderer`:

```kotlin
val renderer = findViewById<SurfaceViewRenderer>(R.id.renderer)
videoTrack.addRenderer(renderer)
// And don't forget to remove it as well after you're done
videoTrack.removeRenderer(renderer)
```

Please note that your `SurfaceViewRenderer` instance must be initialized:

```kotlin
val eglBase = EglBase.create() // Same instance that you passed to WebRtcMediaConnectionFactory
val renderer = findViewById<SurfaceViewRenderer>(R.id.renderer)
// Calling it once in onCreateView() is sufficient
renderer.init(eglBase.eglBaseContext, null)
// Don't forget to release the renderer in onDestroyView()
renderer.release()
```
