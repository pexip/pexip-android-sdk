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
                BluetoothHeadset.STATE_DISCONNECTED
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
