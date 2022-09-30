package com.pexip.sdk.sample.permissions

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.app.ActivityCompat
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class PermissionRationaleHelper @Inject constructor(private val activity: Activity) {

    fun shouldShowRequestPermissionRationale(permission: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}

val LocalPermissionRationaleHelper = staticCompositionLocalOf<PermissionRationaleHelper> {
    error("Must be set explicitly.")
}
