package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Glassmorphism Theme Color Class
class GlassThemeColors(val isDark: Boolean) {
    val textColor: Color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF1F2937) // White / Dark Charcoal
    val secondaryTextColor: Color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280) // Silver Grey / Medium Grey
    val cardBg: Color = if (isDark) Color(0xFF1F2937).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.55f) // Glass frosted effect
    val accentColor: Color = if (isDark) Color(0xFFE5E7EB) else Color(0xFF4B5563) // Silver / Medium Charcoal Accent (No blue!)
    val glassBorder: Color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.5f)
}

@Composable
fun LiquidGlassBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val baseBg = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF111827), // Deep charcoal black
                Color(0xFF1F2937)  // Charcoal grey
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF9FAFB), // Clean white-grey
                Color(0xFFF3F4F6)  // Light grey
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
    ) {
        // Render blurred decorative blobs behind content to simulate "liquid glass"
        if (isDark) {
            // Blob 1: Silver-grey top right
            Box(
                modifier = Modifier
                    .offset(x = 100.dp, y = (-40).dp)
                    .size(280.dp)
                    .blur(70.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4B5563).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Blob 2: Dark Charcoal bottom left
            Box(
                modifier = Modifier
                    .offset(x = (-80).dp, y = 450.dp)
                    .size(320.dp)
                    .blur(80.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF374151).copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Blob 3: Medium Grey near center
            Box(
                modifier = Modifier
                    .offset(x = 180.dp, y = 220.dp)
                    .size(240.dp)
                    .blur(60.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1F2937).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        } else {
            // Light Theme Blobs: White, light silver-grey, pure grey
            // Blob 1: Crisp white blob top right
            Box(
                modifier = Modifier
                    .offset(x = 120.dp, y = (-20).dp)
                    .size(300.dp)
                    .blur(50.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Blob 2: Soft light silver-grey bottom left
            Box(
                modifier = Modifier
                    .offset(x = (-100).dp, y = 400.dp)
                    .size(340.dp)
                    .blur(80.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE5E7EB).copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Blob 3: Medium light grey center right
            Box(
                modifier = Modifier
                    .offset(x = 160.dp, y = 200.dp)
                    .size(250.dp)
                    .blur(65.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD1D5DB).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        content()
    }
}

@Composable
fun GlassmorphicCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    val colors = GlassThemeColors(isDark)
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(1.dp, colors.glassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { content() }
    )
}
