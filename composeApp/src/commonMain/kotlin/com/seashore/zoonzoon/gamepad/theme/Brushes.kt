package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

@Composable
fun zoonZoonAccentBrush(): Brush = Brush.linearGradient(
    colors = listOf(ZoonZoonAccentStart, ZoonZoonAccentEnd),
    start = Offset(0f, 0f),
    end = Offset(400f, 400f)
)

@Composable
fun zoonZoonAccentVerticalBrush(): Brush = Brush.verticalGradient(
    colors = listOf(ZoonZoonAccentStart, ZoonZoonAccentEnd)
)
