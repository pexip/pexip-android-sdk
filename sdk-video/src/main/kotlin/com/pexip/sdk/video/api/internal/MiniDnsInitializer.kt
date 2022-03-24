package com.pexip.sdk.video.api.internal

import android.content.Context
import androidx.startup.Initializer
import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties

internal class MiniDnsInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        AndroidUsingLinkProperties.setup(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
