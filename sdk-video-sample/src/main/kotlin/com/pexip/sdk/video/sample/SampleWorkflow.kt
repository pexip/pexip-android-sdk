package com.pexip.sdk.video.sample

import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.pexip.sdk.workflow.core.Workflow

object SampleWorkflow : Workflow<Unit, SampleOutput, SampleRendering> {

    @Composable
    override fun render(props: Unit, onOutput: (SampleOutput) -> Unit): SampleRendering {
        val regex = remember { Patterns.EMAIL_ADDRESS.toRegex() }
        var value by rememberSaveable { mutableStateOf("") }
        return SampleRendering(
            value = value,
            onValueChange = { value = it.trim() },
            resolveEnabled = regex.matches(value),
            onResolveClick = { onOutput(SampleOutput(value)) }
        )
    }
}

@Immutable
@JvmInline
value class SampleOutput(val uri: String)

@Immutable
data class SampleRendering(
    val value: String,
    val onValueChange: (String) -> Unit,
    val resolveEnabled: Boolean,
    val onResolveClick: () -> Unit,
)
