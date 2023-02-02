/*
 * Copyright 2022-2023 Pexip AS
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

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pexip.sdk.sample.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(rendering: PermissionsRendering, modifier: Modifier = Modifier) {
    BackHandler(onBack = rendering.onBackClick)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name_full))
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
        ) {
            val helper = LocalPermissionRationaleHelper.current
            val contract = ActivityResultContracts.RequestMultiplePermissions()
            val currentOnPermissionsRequestResult by rememberUpdatedState(rendering.onPermissionsRequestResult)
            val launcher = rememberLauncherForActivityResult(contract) {
                Log.e("PermissionsScreen", it.toString())
                val rationales = it.keys.associateWith(helper::shouldShowRequestPermissionRationale)
                Log.e("PermissionsScreen", rationales.toString())
                val result = PermissionsRequestResult(
                    grants = it,
                    rationales = rationales,
                )
                currentOnPermissionsRequestResult(result)
            }
            val permissions = remember(rendering.permissions) {
                rendering.permissions.toTypedArray()
            }
            Text(
                text = stringResource(R.string.permissions_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.permissions_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.permissions_denied_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.permissions_denied_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    launcher.launch(permissions)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.permissions_continue))
            }
        }
    }
}
