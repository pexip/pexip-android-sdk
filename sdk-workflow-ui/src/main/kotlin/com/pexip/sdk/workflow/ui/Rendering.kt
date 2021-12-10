package com.pexip.sdk.workflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Represents a Compose rendering. Implement this by your Rendering type and define how the content
 * should be laid out:
 *
 * ```
 * data class MyRendering(
 *     val enabled: Boolean,
 *     val onEnabledChange: (Boolean) -> Unit
 * ) : Rendering {
 *
 *     @Composable
 *     fun Content(modifier: Modifier) {
 *         MyWidget(
 *             enabled = enabled,
 *             onEnabledChange = onEnabledChange,
 *             modifier = modifier
 *         )
 *     }
 * }
 * ```
 */
fun interface Rendering {

    /**
     * Renders this content.
     *
     * @param modifier a [Modifier] that parent may wish to apply
     */
    @Composable
    fun Content(modifier: Modifier)
}

/**
 * A shorthand for [Content] with an empty [Modifier].
 *
 * @see [Rendering.Content]
 */
@Composable
fun Rendering.Content() = Content(modifier = Modifier)
