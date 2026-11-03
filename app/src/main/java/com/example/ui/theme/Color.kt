package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val BgPrimary = Color(0xFF0B0F19)
val BgSecondary = Color(0xFF131C2E)
val BgSurface = Color(0xFF1E293B)
val BorderColor = Color(0xFF334155)

val TextMain = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)

val AccentPrimary = Color(0xFF6366F1)
val AccentPrimaryHover = Color(0xFF4F46E5)
val AccentSuccess = Color(0xFF10B981)
val AccentDanger = Color(0xFFEF4444)
val AccentWarning = Color(0xFFF59E0B)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    primaryContainer = AccentPrimaryHover,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    background = BgPrimary,
    onBackground = TextMain,
    surface = BgSecondary,
    onSurface = TextMain,
    surfaceVariant = BgSurface,
    onSurfaceVariant = TextMuted,
    outline = BorderColor,
    error = AccentDanger,
    onError = Color.White
)
