@file:OptIn(ExperimentalWorkflowApi::class, ExperimentalWorkflowUiApi::class)

package com.pexip.sdk.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.pexip.sdk.video.node.internal.NodeRenderer
import com.pexip.sdk.video.pin.internal.PinChallengeRenderer
import com.pexip.sdk.video.pin.internal.PinRequirementRenderer
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.ui.ExperimentalWorkflowUiApi
import com.pexip.sdk.workflow.ui.ProvideRendererRegistry
import com.pexip.sdk.workflow.ui.Renderer
import com.pexip.sdk.workflow.ui.RendererRegistry

class ConferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val props = checkNotNull(intent.getParcelableExtra<ConferenceProps>(KEY_CONFERENCE_PROPS))
        val workflow = ConferenceWorkflow()
        setContent {
            ProvideRendererRegistry(RendererRegistry) {
                AppCompatTheme {
                    val rendering = workflow.render(props) {
                        when (it) {
                            is ConferenceOutput.Finish -> finish()
                        }
                    }
                    Renderer(rendering = rendering)
                }
            }
        }
    }

    companion object {

        private const val KEY_CONFERENCE_PROPS = "com.pexip.sdk.video.conference_props"

        private val RendererRegistry = RendererRegistry(
            NodeRenderer.ResolvingNodeRenderer,
            NodeRenderer.FailureRenderer,
            PinRequirementRenderer.ResolvingPinRequirementRenderer,
            PinRequirementRenderer.FailureRenderer,
            PinChallengeRenderer
        )

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
