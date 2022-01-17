package com.pexip.sdk.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.pexip.sdk.video.node.internal.NodeViewFactory
import com.pexip.sdk.video.pin.internal.PinChallengeViewFactory
import com.pexip.sdk.video.pin.internal.PinRequirementViewFactory
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.compose.WorkflowRendering
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ConferenceActivity : AppCompatActivity() {

    private val conferenceViewModel by viewModels<ConferenceViewModel>()

    private val viewRegistry = ViewRegistry(
        NodeViewFactory.ResolvingNodeViewFactory,
        NodeViewFactory.FailureViewFactory,
        PinRequirementViewFactory.ResolvingPinRequirementViewFactory,
        PinRequirementViewFactory.FailureViewFactory,
        PinChallengeViewFactory
    )
    private val viewEnvironment = ViewEnvironment(mapOf(ViewRegistry to viewRegistry))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCompatTheme {
                val rendering by conferenceViewModel.rendering.collectAsState()
                WorkflowRendering(
                    rendering = rendering,
                    viewEnvironment = viewEnvironment
                )
            }
        }
        conferenceViewModel.output
            .onEach(::onConferenceOutput)
            .launchIn(lifecycleScope)
    }

    private fun onConferenceOutput(output: ConferenceOutput) = when (output) {
        ConferenceOutput.Finish -> finish()
    }

    companion object {

        internal const val KEY_CONFERENCE_PROPS = "com.pexip.sdk.video.conference_props"

        @JvmSynthetic
        inline fun start(context: Context, block: ConferenceProps.Builder.() -> Unit) =
            start(context, ConferenceProps(block))

        @JvmStatic
        fun start(context: Context, conferenceProps: ConferenceProps) {
            val intent = Intent(context, ConferenceActivity::class.java)
            intent.putExtra(KEY_CONFERENCE_PROPS, conferenceProps)
            context.startActivity(intent)
        }
    }
}
