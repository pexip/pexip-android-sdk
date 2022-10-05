package com.pexip.sdk.sample

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val color by colors.color(enabled)
    val contentColor by colors.contentColor(enabled)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = IconButtonDefaults.Shape,
        color = color,
        contentColor = contentColor,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.size(IconButtonDefaults.Size),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val color by colors.color(enabled, checked)
    val contentColor by colors.contentColor(enabled, checked)
    Surface(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.semantics { role = Role.Checkbox },
        enabled = enabled,
        shape = IconButtonDefaults.Shape,
        color = color,
        contentColor = contentColor,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.size(IconButtonDefaults.Size),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

object IconButtonDefaults {

    private const val DisabledIconOpacity = 0.38f

    private val IconButtonColor = Color.White.copy(alpha = 0.12f)
    private val IconButtonContentColor = Color.White
    private val CheckedIconButtonColor = Color.White
    private val CheckedIconButtonContentColor = Color.Black

    val Size = 56.dp
    val Shape = CircleShape

    @Composable
    fun iconButtonColors(
        color: Color = IconButtonColor,
        contentColor: Color = IconButtonContentColor,
        disabledColor: Color = color,
        disabledContentColor: Color = contentColor.copy(alpha = DisabledIconOpacity),
    ): IconButtonColors = IconButtonColors(
        color = color,
        contentColor = contentColor,
        disabledColor = disabledColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun iconToggleButtonColors(
        color: Color = IconButtonColor,
        contentColor: Color = IconButtonContentColor,
        disabledColor: Color = color,
        disabledContentColor: Color = contentColor.copy(alpha = DisabledIconOpacity),
        checkedColor: Color = CheckedIconButtonColor,
        checkedContentColor: Color = CheckedIconButtonContentColor,
    ): IconToggleButtonColors = IconToggleButtonColors(
        color = color,
        contentColor = contentColor,
        disabledColor = disabledColor,
        disabledContentColor = disabledContentColor,
        checkedColor = checkedColor,
        checkedContentColor = checkedContentColor
    )
}

data class IconButtonColors(
    val color: Color,
    val contentColor: Color,
    val disabledColor: Color,
    val disabledContentColor: Color,
) {

    @Composable
    fun color(enabled: Boolean): State<Color> =
        animateColorAsState(if (enabled) color else disabledColor)

    @Composable
    fun contentColor(enabled: Boolean): State<Color> =
        animateColorAsState(if (enabled) contentColor else disabledContentColor)
}

data class IconToggleButtonColors(
    val color: Color,
    val contentColor: Color,
    val disabledColor: Color,
    val disabledContentColor: Color,
    val checkedColor: Color,
    val checkedContentColor: Color,
) {

    @Composable
    fun color(enabled: Boolean, checked: Boolean): State<Color> {
        val target = when {
            !enabled -> disabledColor
            !checked -> color
            else -> checkedColor
        }
        return animateColorAsState(target)
    }

    @Composable
    fun contentColor(enabled: Boolean, checked: Boolean): State<Color> {
        val target = when {
            !enabled -> disabledContentColor
            !checked -> contentColor
            else -> checkedContentColor
        }
        return animateColorAsState(target)
    }
}
