package com.pexip.sdk.video.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.pexip.sdk.video.ConferenceActivity
import com.pexip.sdk.workflow.core.render
import com.pexip.sdk.workflow.ui.ProvideRendererRegistry
import com.pexip.sdk.workflow.ui.Renderer
import com.pexip.sdk.workflow.ui.RendererRegistry

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCompatTheme {
                ProvideRendererRegistry(SampleRendererRegistry) {
                    val context = LocalContext.current
                    val rendering = SampleWorkflow.render {
                        ConferenceActivity.start(context) {
                            uri(it.uri)
                            displayName("Pexip Video SDK")
                        }
                    }
                    Renderer(rendering = rendering)
                }
            }
        }
    }
}

private val SampleRendererRegistry = RendererRegistry(SampleRenderer)
