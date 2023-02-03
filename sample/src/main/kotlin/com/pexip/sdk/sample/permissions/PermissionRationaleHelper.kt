/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
