package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val LightBackground = Color(0xFFF5F5F7)
val LightSurface = Color(0xFFFFFFFF)
val LightPrimary = Color(0xFF2196F3)
val LightSecondary = Color(0xFF00796B)
val LightOnBackground = Color(0xFF1E1E1E)
val LightOnSurface = Color(0xFF212121)

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkPrimary = Color(0xFF1DE9B6)
val DarkSecondary = Color(0xFF80CBC4)
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkOnSurface = Color(0xFFF5F5F5)

// Note tints
val NoteTintCream = Color(0xFFF6E7B2)
val NoteTintMint = Color(0xFFD1F2D9)
val NoteTintRose = Color(0xFFFFD6E0)
val NoteTintLavender = Color(0xFFE8D7FF)
val NoteTintSky = Color(0xFFD0E8FF)
val NoteTintTeal = Color(0xFF1E5B5B)
val NoteTintWhite = Color(0xFFFFFFFF)

// Focus mode color
val FocusModeBg = Color(0xFF1E5B5B)
val VipOrangeGradientStart = Color(0xFFFF9800)
val VipOrangeGradientEnd = Color(0xFFFF5722)

fun getNoteColor(tint: String, isDark: Boolean = false): Color {
    return when (tint.lowercase()) {
        "cream" -> if (isDark) Color(0xFF4A4432) else NoteTintCream
        "mint" -> if (isDark) Color(0xFF2A4232) else NoteTintMint
        "rose" -> if (isDark) Color(0xFF4A2F38) else NoteTintRose
        "lavender" -> if (isDark) Color(0xFF382F48) else NoteTintLavender
        "sky" -> if (isDark) Color(0xFF293B4E) else NoteTintSky
        "teal" -> NoteTintTeal
        else -> if (isDark) DarkSurface else LightSurface
    }
}
