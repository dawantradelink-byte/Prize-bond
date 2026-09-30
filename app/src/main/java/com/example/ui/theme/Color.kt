package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// MotionSites.ai Cyber & Obsidian Canvas Palette
val MotionCanvasDark = Color(0xFF080A10)
val MotionSurfaceDark = Color(0xFF0E131F)
val MotionSurfaceDarkElevated = Color(0xFF141B2D)
val MotionSurfaceDarkCard = Color(0xFF192238)
val MotionGlassDark = Color(0x99141B2D)
val MotionBorderDark = Color(0x338B5CF6)
val MotionBorderGlow = Color(0x6606B6D4)
val MotionHairlineDark = Color(0xFF232D47)

// Studio Light Mode (High Contrast Sleek Tech)
val MotionCanvasLight = Color(0xFFF6F8FC)
val MotionSurfaceLight = Color(0xFFFFFFFF)
val MotionSurfaceLightCard = Color(0xFFEEF2F9)
val MotionSurfaceLightElevated = Color(0xFFE2E8F4)
val MotionHairlineLight = Color(0xFFDCE3F0)

// Legacy Aliases for seamless compatibility
val CanvasCream = MotionCanvasLight
val SurfaceSoft = MotionSurfaceLightCard
val SurfaceCard = MotionSurfaceLightCard
val SurfaceCreamStrong = MotionSurfaceLightElevated
val Hairline = MotionHairlineLight
val HairlineSoft = Color(0xFFE4EBF6)

// Neon & Cyber Accent Colors
val CyberViolet = Color(0xFF8B5CF6)
val CyberVioletDark = Color(0xFF6D28D9)
val CyberCyan = Color(0xFF06B6D4)
val CyberCyanLight = Color(0xFF67E8F9)
val CyberCoral = Color(0xFFFF5757)
val CyberRose = Color(0xFFF43F5E)
val CyberEmerald = Color(0xFF10B981)
val CyberAmber = Color(0xFFF59E0B)

// Primary Coral Accents
val CoralPrimary = Color(0xFFFF5757)
val CoralActive = Color(0xFFE11D48)
val CoralDisabled = Color(0x4DFF5757)

// Luxury & Royal Gold Palette (Winning & Prize Bonds)
val GoldPrimary = Color(0xFFF59E0B)
val GoldLight = Color(0xFFFDE68A)
val GoldSuperLight = Color(0xFFFFFBEB)
val GoldAmber = Color(0xFFD97706)
val GoldDark = Color(0xFF92400E)
val RoyalGold = Color(0xFFF59E0B)
val RoyalGoldDark = Color(0xFF451A03)

// Text & Ink (High Contrast Cyber / Charcoal)
val InkPrimary = Color(0xFF0F172A)
val BodyStrong = Color(0xFF1E293B)
val BodyRegular = Color(0xFF334155)
val TextMuted = Color(0xFF64748B)
val BodyMuted = Color(0xFF64748B)
val TextMutedSoft = Color(0xFF94A3B8)

// Dark Surfaces (Product Chrome / Dark Mode)
val SurfaceDark = MotionSurfaceDark
val SurfaceDarkElevated = MotionSurfaceDarkElevated
val SurfaceDarkSoft = MotionSurfaceDarkCard
val DarkHairline = MotionHairlineDark
val OnDark = Color(0xFFF8FAFC)
val OnDarkSoft = Color(0xFF94A3B8)

// Semantic Accents
val EmeraldSuccess = Color(0xFF10B981)
val EmeraldLight = Color(0xFFD1FAE5)
val EmeraldAccent = Color(0xFF10B981)
val TealAccent = Color(0xFF06B6D4)
val ErrorRed = Color(0xFFEF4444)

// Glassmorphism & Frosted Glass Tints
val GlassSurfaceDark = Color(0x990E131F)
val GlassSurfaceDarkElevated = Color(0xB3141B2D)
val GlassSurfaceLight = Color(0xB3FFFFFF)
val GlassSurfaceLightCard = Color(0xCCF6F8FC)
val GlassBorderDark = Color(0x338B5CF6)
val GlassBorderLight = Color(0x40DCE3F0)
val GlassHighlightWhite = Color(0x26FFFFFF)

// Core Brand Gradients
val CyberGradient = androidx.compose.ui.graphics.Brush.linearGradient(listOf(CyberViolet, CyberCyan))
val CyberCoralGradient = androidx.compose.ui.graphics.Brush.linearGradient(listOf(CyberViolet, CyberCoral))
val GoldFlameGradient = androidx.compose.ui.graphics.Brush.linearGradient(listOf(GoldPrimary, CyberAmber, CyberCoral))
val EmeraldCyanGradient = androidx.compose.ui.graphics.Brush.linearGradient(listOf(CyberEmerald, CyberCyan))
val GlassBorderGradientDark = androidx.compose.ui.graphics.Brush.linearGradient(
  listOf(
    Color(0x55FFFFFF),
    Color(0x338B5CF6),
    Color(0x4D06B6D4),
    Color(0x1AFFFFFF)
  )
)
val GlassBorderGradientLight = androidx.compose.ui.graphics.Brush.linearGradient(
  listOf(
    Color(0xB3FFFFFF),
    Color(0x4D8B5CF6),
    Color(0x6606B6D4),
    Color(0x33DCE3F0)
  )
)

