package com.pexip.sdk.media.webrtc

import android.content.Context
import android.util.AttributeSet
import com.pexip.sdk.media.Renderer

public class SurfaceViewRenderer : org.webrtc.SurfaceViewRenderer, Renderer {

    public constructor(context: Context) : super(context)

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
}
