package com.pexip.sdk.video.sample

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.pexip.sdk.video.sample.alias.AliasViewFactory
import com.pexip.sdk.video.sample.conference.ConferenceViewFactory
import com.pexip.sdk.video.sample.node.NodeViewFactory
import com.pexip.sdk.video.sample.pinchallenge.PinChallengeViewFactory
import com.pexip.sdk.video.sample.pinrequirement.PinRequirementViewFactory
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.compose.WorkflowRendering
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SampleActivity : AppCompatActivity() {

    private val sampleViewModel by viewModels<SampleViewModel>()

    private val viewRegistry = ViewRegistry(
        AliasViewFactory,
        NodeViewFactory.ResolvingNodeViewFactory,
        NodeViewFactory.FailureViewFactory,
        PinChallengeViewFactory,
        PinRequirementViewFactory.ResolvingPinRequirementViewFactory,
        PinRequirementViewFactory.FailureViewFactory,
        ConferenceViewFactory
    )
    private val viewEnvironment = ViewEnvironment(mapOf(ViewRegistry to viewRegistry))
    private val launcher = registerForActivityResult(RequestMultiplePermissions()) {
        if (it.any { (_, granted) -> !granted }) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCompatTheme {
                val rendering by sampleViewModel.rendering.collectAsState()
                WorkflowRendering(
                    rendering = rendering,
                    viewEnvironment = viewEnvironment
                )
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
