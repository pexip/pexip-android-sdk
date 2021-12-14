package com.pexip.sdk.workflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlin.reflect.KClass

/**
 * A type aware of how a [Rendering] should be laid out.
 *
 * An implementation may look as follows:
 *
 * ```
 * object MyRenderer : Renderer<MyRendering> {
 *
 *     val type: KClass<in MyRendering> = MyRendering::class
 *
 *     @Composable
 *     fun MyRendering.Content(modifier: Modifier) {
 *         MyWidget(
 *             enabled = enabled,
 *             onEnabledChange = onEnabledChange,
 *             modifier = modifier
 *         )
 *     }
 * }
 * ```
 */
@ExperimentalWorkflowUiApi
interface Renderer<in Rendering : Any> {

    /**
     * A type of this [Rendering]. May be used as a key.
     */
    @ExperimentalWorkflowUiApi
    val type: KClass<in Rendering>

    /**
     * Renders this content.
     *
     * @param modifier a [Modifier] that parent may wish to apply
     */
    @ExperimentalWorkflowUiApi
    @Composable
    fun Rendering.Content(modifier: Modifier)
}

/**
 * Renders the supplied rendering.
 *
 * @param rendering a rendering
 * @param modifier a [Modifier] that parent may wish to apply
 * @param registry a [RendererRegistry] used to retrieve the [Renderer]
 *
 * @throws NoSuchElementException
 */
@ExperimentalWorkflowUiApi
@Composable
fun Renderer(
    rendering: Any,
    modifier: Modifier = Modifier,
    registry: RendererRegistry = LocalRendererRegistry.current,
) {
    val renderer = remember(registry, rendering::class) { registry[rendering::class] }
    Renderer(
        rendering = rendering,
        renderer = renderer,
        modifier = modifier
    )
}

/**
 * Renders the supplied rendering using a provide renderer.
 *
 * @param rendering a rendering
 * @param renderer a renderer that will draw the content
 * @param modifier a [Modifier] that parent may wish to apply
 */
@ExperimentalWorkflowUiApi
@Composable
fun <Rendering : Any> Renderer(
    rendering: Rendering,
    renderer: Renderer<Rendering>,
    modifier: Modifier = Modifier,
) {
    with(renderer) { rendering.Content(modifier) }
}

/**
 * A shorthand for creating a renderer for a particular type.
 *
 * An implementation may look as follows:
 *
 * ```
 * val MyRenderer = renderer<MyRendering> { modifier ->
 *     MyWidget(
 *         enabled = enabled,
 *         onEnabledChange = onEnabledChange,
 *         modifier = modifier
 *     )
 * }
 *
 * @param content a content to render
 */
@ExperimentalWorkflowUiApi
inline fun <reified Rendering : Any> renderer(noinline content: @Composable Rendering.(Modifier) -> Unit): Renderer<Rendering> =
    renderer(Rendering::class, content)

@ExperimentalWorkflowUiApi
@PublishedApi
internal fun <Rendering : Any> renderer(
    type: KClass<Rendering>,
    content: @Composable (Rendering.(Modifier) -> Unit),
): Renderer<Rendering> = object : Renderer<Rendering> {

    override val type: KClass<in Rendering> = type

    @Composable
    override fun Rendering.Content(modifier: Modifier) {
        content(modifier)
    }
}
