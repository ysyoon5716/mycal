package com.example.mycal.presentation.screens.subscription.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFFE57373), // Red
        Color(0xFFF06292), // Pink
        Color(0xFFBA68C8), // Purple
        Color(0xFF9575CD), // Deep Purple
        Color(0xFF7986CB), // Indigo
        Color(0xFF64B5F6), // Blue
        Color(0xFF4FC3F7), // Light Blue
        Color(0xFF4DD0E1), // Cyan
        Color(0xFF4DB6AC), // Teal
        Color(0xFF81C784), // Green
        Color(0xFFAED581), // Light Green
        Color(0xFFDCE775), // Lime
        Color(0xFFFFD54F), // Amber
        Color(0xFFFFB74D), // Orange
        Color(0xFFFF8A65), // Deep Orange
        Color(0xFFA1887F)  // Brown
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Calendar Color",
            style = MaterialTheme.typography.labelMedium
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (color == selectedColor) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (color == selectedColor) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}