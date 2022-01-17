package com.pexip.sdk.video.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.pexip.sdk.video.ConferenceActivity
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.compose.WorkflowRendering
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SampleActivity : AppCompatActivity() {

    private val sampleViewModel by viewModels<SampleViewModel>()

    private val viewRegistry = ViewRegistry(SampleViewFactory)
    private val viewEnvironment = ViewEnvironment(mapOf(ViewRegistry to viewRegistry))

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
    }

    private fun onSampleOutput(output: SampleOutput) = ConferenceActivity.start(this) {
        alias(output.uri)
        displayName("Pexip Video SDK")
    }
}
