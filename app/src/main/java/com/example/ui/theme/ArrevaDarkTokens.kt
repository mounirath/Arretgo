package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * arreva GPS Dark Navy & Amber Design System Tokens
 * Exact match to user reference screenshots (Screenshot_2026-10-06-07-23-46-85 & Screenshot_2026-10-06-07-23-32-82)
 */
object ArrevaDarkTokens {
    // Backgrounds & Surfaces (Deep Midnight Navy)
    val NavyBackground = Color(0xFF0A1120)
    val NavySurface = Color(0xFF0D1527)
    val NavySheet = Color(0xFF0E172B)
    val NavyCard = Color(0xFF162238)
    val NavyCardActive = Color(0xFF1E2D4A)
    val NavyBorder = Color(0xFF22324C)
    val NavyBorderSubtle = Color(0xFF1A263C)

    // Primary Accents (Warm Golden Amber & Ochre)
    val AmberPrimary = Color(0xFFD97706) // CTA Button background
    val AmberLight = Color(0xFFF59E0B)   // Language active badge, links, compass disc
    val AmberGlow = Color(0xFFFBBF24)    // Highlights & ratings
    val TextOnAmber = Color(0xFF18181B)  // High contrast dark text on golden button

    // Status Colors
    val EmeraldGps = Color(0xFF10B981)   // GPS active indicator
    val EmeraldBg = Color(0xFF063B2F)    // GPS badge capsule
    val AlertRed = Color(0xFFEF4444)     // Alert zone breached
    val AlertBg = Color(0xFF450A0A)

    // Text & Icons
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextTertiary = Color(0xFF64748B)
    val TextLightSlate = Color(0xFFCBD5E1)

    // Icon Container Backgrounds (Menu items in sidebar)
    val IconBgBlue = Color(0xFF1E3A8A)
    val IconBgAmber = Color(0xFF78350F)
    val IconBgGold = Color(0xFF713F12)
    val IconBgTeal = Color(0xFF064E3B)
    val IconBgPurple = Color(0xFF581C87)
    val IconBgEmerald = Color(0xFF065F46)
}
