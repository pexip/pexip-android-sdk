package com.pexip.sdk.workflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.reflect.KClass

/**
 * A registry of all available renderers.
 */
@ExperimentalWorkflowUiApi
interface RendererRegistry {

    /**
     * Retrieves the renderer based on rendering type.
     *
     * @param type a rendering class
     * @return a renderer
     *
     * @throws NoSuchElementException if renderer for [type] doesn't exist
     */
    @ExperimentalWorkflowUiApi
    operator fun <Rendering : Any> get(type: KClass<out Rendering>): Renderer<Rendering>
}

/**
 * Creates a registry of supplied renderers.
 *
 * @param renderers an array of renderers that will make up this registry
 * @return a registry
 *
 * @throws IllegalArgumentException if multiple renderers for the same type were provided
 */
@ExperimentalWorkflowUiApi
fun RendererRegistry(vararg renderers: Renderer<*>): RendererRegistry {
    val map = renderers.associateBy { it.type }
    require(map.size == renderers.size) { "Multiple renderers for the same type detected." }
    return RendererRegistry(map)
}

/**
 * Creates a registry of supplied renderers.
 *
 * @param renderers an map of renderers that will make up this registry
 * @return a registry
 */
@ExperimentalWorkflowUiApi
fun RendererRegistry(renderers: Map<KClass<*>, Renderer<*>>): RendererRegistry =
    object : RendererRegistry {

        @Suppress("UNCHECKED_CAST")
        override fun <Rendering : Any> get(type: KClass<out Rendering>): Renderer<Rendering> =
            renderers[type] as? Renderer<Rendering>
                ?: throw NoSuchElementException("No renderer for type $type.")
    }

/**
 * Provides a renderer registry in [content] lambda.
 *
 * @param registry a registry
 * @param content a Composable content
 */
@ExperimentalWorkflowUiApi
@Composable
fun ProvideRendererRegistry(registry: RendererRegistry, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalRendererRegistry provides registry, content = content)
}

@ExperimentalWorkflowUiApi
internal val LocalRendererRegistry = staticCompositionLocalOf<RendererRegistry> {
    error("RendererRegistry is not set.")
}
