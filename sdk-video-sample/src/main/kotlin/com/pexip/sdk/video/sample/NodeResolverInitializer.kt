package com.pexip.sdk.video.sample

import android.content.Context
import androidx.startup.Initializer
import com.pexip.sdk.api.infinity.NodeResolver

class NodeResolverInitializer : Initializer<Unit> {

    override fun create(context: Context) = NodeResolver.initialize(context)

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
