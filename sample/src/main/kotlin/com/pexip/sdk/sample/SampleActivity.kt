package com.pexip.sdk.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.pexip.sdk.media.webrtc.compose.LocalEglBase
import com.pexip.sdk.sample.permissions.LocalPermissionRationaleHelper
import com.pexip.sdk.sample.permissions.PermissionRationaleHelper
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.WorkflowRendering
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.webrtc.EglBase
import javax.inject.Inject

@AndroidEntryPoint
class SampleActivity : AppCompatActivity() {

    @Inject
    lateinit var eglBase: EglBase

    @Inject
    lateinit var viewEnvironment: ViewEnvironment

    @Inject
    lateinit var permissionRationaleHelper: PermissionRationaleHelper

    private val sampleViewModel by viewModels<SampleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CompositionLocalProvider(
                LocalEglBase provides eglBase,
                LocalPermissionRationaleHelper provides permissionRationaleHelper,
            ) {
                SampleTheme {
                    val rendering by sampleViewModel.rendering.collectAsState()
                    WorkflowRendering(
                        rendering = rendering,
                        viewEnvironment = viewEnvironment,
                    )
                }
            }
        }
        sampleViewModel.output
            .onEach(::onSampleOutput)
            .launchIn(lifecycleScope)
    }

    private fun onSampleOutput(output: SampleOutput) = when (output) {
        is SampleOutput.Toast -> Toast.makeText(this, output.message, Toast.LENGTH_SHORT).show()
        is SampleOutput.ApplicationDetailsSettings -> {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null),
            )
            startActivity(intent)
        }
        is SampleOutput.Finish -> finish()
    }
}
