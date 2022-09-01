package com.pexip.sdk.sample

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.pexip.sdk.media.webrtc.compose.LocalEglBase
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

    private val sampleViewModel by viewModels<SampleViewModel>()

    private val launcher = registerForActivityResult(RequestMultiplePermissions()) {
        if (it.any { (_, granted) -> !granted }) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalEglBase provides eglBase) {
                SampleTheme {
                    val rendering by sampleViewModel.rendering.collectAsState()
                    WorkflowRendering(
                        rendering = rendering,
                        viewEnvironment = viewEnvironment
                    )
                }
            }
        }
        sampleViewModel.output
            .onEach(::onSampleOutput)
            .launchIn(lifecycleScope)
        launcher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))
    }

    private fun onSampleOutput(output: SampleOutput) = when (output) {
        is SampleOutput.Toast -> Toast.makeText(this, output.message, Toast.LENGTH_SHORT).show()
        is SampleOutput.Finish -> finish()
    }
}
