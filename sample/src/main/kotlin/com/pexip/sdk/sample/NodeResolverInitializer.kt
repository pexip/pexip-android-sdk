package com.pexip.sdk.sample

import android.content.Context
import androidx.startup.Initializer
import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties

class NodeResolverInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        AndroidUsingLinkProperties.setup(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
