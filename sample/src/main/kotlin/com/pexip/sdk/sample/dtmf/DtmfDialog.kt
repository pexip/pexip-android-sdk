package com.pexip.sdk.sample.dtmf

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DtmfDialog(rendering: DtmfRendering) {
    Dialog(onDismissRequest = rendering.onBackClick) {
        Surface(shape = TonePadShape) {
            TonePad(rendering = rendering)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TonePad(rendering: DtmfRendering, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier
    ) {
        items(Tones) {
            ToneButton(
                tone = it,
                onClick = { rendering.onToneClick(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ToneButton(tone: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = ToneButtonShape,
        color = MaterialTheme.colors.primary,
        elevation = 4.dp,
        role = Role.Button,
        modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = tone)
        }
    }
}

private val Tones = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
private val TonePadShape = RoundedCornerShape(8.dp)
private val ToneButtonShape = RoundedCornerShape(4.dp)
