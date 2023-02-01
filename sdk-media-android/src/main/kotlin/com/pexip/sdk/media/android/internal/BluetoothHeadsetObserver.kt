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
package com.pexip.sdk.media.android.internal

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import java.io.Closeable

internal class BluetoothHeadsetObserver(
    private val context: Context,
    handler: Handler,
    private val onConnectedChange: (connected: Boolean, name: String?) -> Unit,
) : Closeable, BluetoothProfile.ServiceListener {

    private val filter = IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)

    @SuppressLint("MissingPermission")
    private val receiver = context.registerReceiver(filter, handler) { context, intent ->
        if (context.checkBluetoothConnectPermission() == PackageManager.PERMISSION_GRANTED) {
            val state = intent.getIntExtra(
                BluetoothHeadset.EXTRA_STATE,
                BluetoothHeadset.STATE_DISCONNECTED,
            )
            val device =
                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            onConnectedChange(state == BluetoothHeadset.STATE_CONNECTED, device?.name)
        }
    }
    private val manager = context.getSystemService<BluetoothManager>()
    private val adapter = manager?.adapter
    private var proxy: BluetoothHeadset? = null

    init {
        adapter?.getProfileProxy(context, this, BluetoothProfile.HEADSET)
    }

    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        requireBluetoothHeadsetProfile(profile)
        this.proxy = proxy as BluetoothHeadset
        if (context.checkBluetoothConnectPermission() == PackageManager.PERMISSION_GRANTED) {
            val device = proxy.connectedDevices.firstOrNull()
            if (device != null) onConnectedChange(true, device.name)
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        requireBluetoothHeadsetProfile(profile)
        onConnectedChange(false, null)
    }

    override fun close() {
        adapter?.closeProfileProxy(BluetoothProfile.HEADSET, proxy)
        proxy = null
        receiver.close()
    }

    private fun requireBluetoothHeadsetProfile(profile: Int) =
        require(profile == BluetoothProfile.HEADSET)

    private fun Context.checkBluetoothConnectPermission(): Int {
        val permission = when {
            Build.VERSION.SDK_INT >= 31 -> Manifest.permission.BLUETOOTH_CONNECT
            else -> Manifest.permission.BLUETOOTH
        }
        return ContextCompat.checkSelfPermission(this, permission)
    }
}
