package com.pexip.sdk.sample.dtmf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DtmfDialog(rendering: DtmfRendering) {
    Dialog(onDismissRequest = rendering.onBackClick) {
        Surface(shape = TonePadShape) {
            TonePad(rendering = rendering)
        }
    }
}

@Composable
private fun TonePad(rendering: DtmfRendering, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToneButton(tone: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = ToneButtonShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
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
