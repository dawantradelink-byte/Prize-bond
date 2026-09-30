package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCoral
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.GlassBorderGradientDark
import com.example.ui.theme.GlassBorderGradientLight
import com.example.ui.theme.GlassSurfaceDarkElevated
import com.example.ui.theme.GlassSurfaceLightCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MotionCanvasDark
import com.example.ui.theme.MotionCanvasLight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Atmospheric background featuring a smooth, continuous looping animation
 * with orbiting luminous gradient nebulae, floating stardust particles,
 * and an undulating cyber aurora ribbon.
 */
@Composable
fun CyberMeshBackground(
  isDarkMode: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "smooth_background_loop")

  // 1. Continuous 360-degree orbital rotation for Orb 1 (Clockwise)
  val primaryRotationProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 20000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "primary_orbit"
  )

  // 2. Continuous 360-degree orbital rotation for Orb 2 (Counter-Clockwise)
  val secondaryRotationProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 28000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "secondary_orbit"
  )

  // 3. Continuous linear wave flow for aurora ribbon and floating stardust
  val waveFlowProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 14000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_flow"
  )

  // 4. Subtle slow breathing pulse for luminous radii and glow intensity
  val breathingPulse by infiniteTransition.animateFloat(
    initialValue = 0.88f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathing_pulse"
  )

  val baseBg = if (isDarkMode) MotionCanvasDark else MotionCanvasLight

  // Pre-configured floating stardust seeds (u, v, sizeDp, speedMultiplier, baseAlpha)
  val stardustSeeds = rememberStardustSeeds()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(baseBg)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      val twoPi = (2.0 * Math.PI).toFloat()

      // ========================================================
      // 1. ORBITING NEBULA 1: Cyber Violet & Cyan (Top Right Area)
      // ========================================================
      val angle1 = primaryRotationProgress * twoPi
      val orbitRadiusX1 = canvasWidth * 0.24f
      val orbitRadiusY1 = canvasHeight * 0.16f
      val centerOrb1X = canvasWidth * 0.72f + orbitRadiusX1 * cos(angle1)
      val centerOrb1Y = canvasHeight * 0.20f + orbitRadiusY1 * sin(angle1)
      val radiusOrb1 = canvasWidth * 0.68f * breathingPulse

      val orb1CoreColor = if (isDarkMode) CyberCyan.copy(alpha = 0.24f) else CyberCyan.copy(alpha = 0.14f)
      val orb1OuterColor = if (isDarkMode) CyberViolet.copy(alpha = 0.28f) else CyberViolet.copy(alpha = 0.16f)

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(orb1CoreColor, orb1OuterColor, Color.Transparent),
          center = Offset(centerOrb1X, centerOrb1Y),
          radius = radiusOrb1
        ),
        radius = radiusOrb1,
        center = Offset(centerOrb1X, centerOrb1Y)
      )

      // ========================================================
      // 2. ORBITING NEBULA 2: Coral & Gold Amber (Bottom Left Area)
      // ========================================================
      val angle2 = -secondaryRotationProgress * twoPi
      val orbitRadiusX2 = canvasWidth * 0.22f
      val orbitRadiusY2 = canvasHeight * 0.18f
      val centerOrb2X = canvasWidth * 0.28f + orbitRadiusX2 * cos(angle2)
      val centerOrb2Y = canvasHeight * 0.72f + orbitRadiusY2 * sin(angle2)
      val radiusOrb2 = canvasWidth * 0.72f * (2f - breathingPulse)

      val orb2CoreColor = if (isDarkMode) GoldPrimary.copy(alpha = 0.22f) else CyberAmber.copy(alpha = 0.12f)
      val orb2OuterColor = if (isDarkMode) CyberCoral.copy(alpha = 0.26f) else CyberCoral.copy(alpha = 0.14f)

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(orb2CoreColor, orb2OuterColor, Color.Transparent),
          center = Offset(centerOrb2X, centerOrb2Y),
          radius = radiusOrb2
        ),
        radius = radiusOrb2,
        center = Offset(centerOrb2X, centerOrb2Y)
      )

      // ========================================================
      // 3. FLOATING NEBULA 3: Cyber Emerald Pulse (Mid-Center)
      // ========================================================
      val angle3 = (primaryRotationProgress + 0.5f) * twoPi
      val centerOrb3X = canvasWidth * 0.50f + (canvasWidth * 0.18f) * sin(angle3)
      val centerOrb3Y = canvasHeight * 0.48f + (canvasHeight * 0.12f) * cos(angle3)
      val radiusOrb3 = canvasWidth * 0.52f * breathingPulse

      val orb3Color = if (isDarkMode) CyberEmerald.copy(alpha = 0.14f) else CyberEmerald.copy(alpha = 0.08f)

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(orb3Color, Color.Transparent),
          center = Offset(centerOrb3X, centerOrb3Y),
          radius = radiusOrb3
        ),
        radius = radiusOrb3,
        center = Offset(centerOrb3X, centerOrb3Y)
      )

      // ========================================================
      // 4. UNDULATING CYBER AURORA WAVE RIBBON
      // ========================================================
      val waveOffset = waveFlowProgress * twoPi
      val wavePath = Path()
      val waveBaseY = canvasHeight * 0.52f
      val waveAmplitude = 45.dp.toPx()

      wavePath.moveTo(0f, waveBaseY + sin(waveOffset) * waveAmplitude)

      var currentX = 0f
      val stepX = 30f
      while (currentX <= canvasWidth) {
        val normalizedX = currentX / canvasWidth
        val y = waveBaseY + sin(normalizedX * twoPi * 2f + waveOffset) * waveAmplitude +
            cos(normalizedX * twoPi * 1.5f + waveOffset * 0.7f) * (waveAmplitude * 0.5f)
        wavePath.lineTo(currentX, y)
        currentX += stepX
      }

      val auroraGradient = Brush.horizontalGradient(
        colors = listOf(
          CyberViolet.copy(alpha = if (isDarkMode) 0.18f else 0.10f),
          CyberCyan.copy(alpha = if (isDarkMode) 0.24f else 0.14f),
          CyberCoral.copy(alpha = if (isDarkMode) 0.16f else 0.08f),
          CyberViolet.copy(alpha = if (isDarkMode) 0.18f else 0.10f)
        )
      )

      drawPath(
        path = wavePath,
        brush = auroraGradient,
        style = Stroke(width = 3.dp.toPx())
      )

      // ========================================================
      // 5. SEAMLESSLY LOOPING FLOATING STARDUST PARTICLES
      // ========================================================
      for (seed in stardustSeeds) {
        val particleX = seed.relX * canvasWidth
        // Smooth vertical upward drift with wrap-around
        val rawY = seed.relY - (waveFlowProgress * seed.speedFactor)
        val wrappedY = ((rawY % 1f + 1f) % 1f) * canvasHeight

        val particleRadius = seed.sizeDp.dp.toPx() * (0.8f + 0.4f * sin(angle1 + seed.phase))
        val particleAlpha = seed.baseAlpha * if (isDarkMode) 1.2f else 0.7f

        val particleColor = when (seed.colorIndex) {
          0 -> CyberCyan
          1 -> CyberViolet
          2 -> GoldPrimary
          else -> CyberCoral
        }.copy(alpha = particleAlpha)

        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(particleColor, Color.Transparent),
            center = Offset(particleX, wrappedY),
            radius = particleRadius * 2.2f
          ),
          radius = particleRadius * 2.2f,
          center = Offset(particleX, wrappedY)
        )
      }

      // ========================================================
      // 6. DELICATE CYBER GRID PATTERN
      // ========================================================
      val gridSpacing = 48.dp.toPx()
      val gridLineColor = if (isDarkMode) Color(0x0A8B5CF6) else Color(0x0864748B)

      var gx = 0f
      while (gx <= canvasWidth) {
        drawLine(
          color = gridLineColor,
          start = Offset(gx, 0f),
          end = Offset(gx, canvasHeight),
          strokeWidth = 1f
        )
        gx += gridSpacing
      }

      var gy = 0f
      while (gy <= canvasHeight) {
        drawLine(
          color = gridLineColor,
          start = Offset(0f, gy),
          end = Offset(canvasWidth, gy),
          strokeWidth = 1f
        )
        gy += gridSpacing
      }
    }

    // 2. Layered Screen Content
    content()
  }
}

private data class StardustSeed(
  val relX: Float,
  val relY: Float,
  val sizeDp: Float,
  val speedFactor: Float,
  val baseAlpha: Float,
  val colorIndex: Int,
  val phase: Float
)

private fun rememberStardustSeeds(): List<StardustSeed> {
  return listOf(
    StardustSeed(0.12f, 0.20f, 3.0f, 0.4f, 0.35f, 0, 0.2f),
    StardustSeed(0.85f, 0.35f, 4.0f, 0.6f, 0.40f, 1, 1.4f),
    StardustSeed(0.25f, 0.60f, 2.5f, 0.5f, 0.30f, 2, 2.8f),
    StardustSeed(0.70f, 0.75f, 3.5f, 0.7f, 0.38f, 0, 4.1f),
    StardustSeed(0.40f, 0.15f, 2.0f, 0.3f, 0.25f, 3, 0.9f),
    StardustSeed(0.92f, 0.82f, 3.2f, 0.5f, 0.32f, 1, 3.3f),
    StardustSeed(0.18f, 0.90f, 2.8f, 0.6f, 0.36f, 2, 5.0f),
    StardustSeed(0.55f, 0.40f, 4.2f, 0.4f, 0.42f, 0, 1.9f),
    StardustSeed(0.32f, 0.85f, 2.2f, 0.7f, 0.28f, 1, 2.4f),
    StardustSeed(0.78f, 0.10f, 3.0f, 0.5f, 0.34f, 3, 4.6f),
    StardustSeed(0.08f, 0.50f, 3.8f, 0.3f, 0.37f, 0, 0.7f),
    StardustSeed(0.62f, 0.92f, 2.6f, 0.6f, 0.30f, 2, 3.7f)
  )
}

/**
 * Modifier to easily make any Box or Card glassmorphic with frosted transparency
 * and a luminous gradient hairline border.
 */
fun Modifier.glassmorphic(
  isDarkMode: Boolean,
  shape: Shape = RoundedCornerShape(16.dp),
  borderBrush: Brush? = null,
  borderWidth: Dp = 1.dp
): Modifier = this
  .clip(shape)
  .background(
    if (isDarkMode) GlassSurfaceDarkElevated else GlassSurfaceLightCard
  )
  .border(
    width = borderWidth,
    brush = borderBrush ?: if (isDarkMode) GlassBorderGradientDark else GlassBorderGradientLight,
    shape = shape
  )

/**
 * Reusable Glassmorphic Container Card with high-precision frosted aesthetics.
 */
@Composable
fun GlassmorphicCard(
  isDarkMode: Boolean,
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  borderBrush: Brush? = null,
  padding: Dp = 16.dp,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .glassmorphic(
        isDarkMode = isDarkMode,
        shape = shape,
        borderBrush = borderBrush
      )
      .padding(padding),
    content = content
  )
}

/**
 * Gradient text styling for impactful statistics and headings.
 */
@Composable
fun GradientText(
  text: String,
  gradient: Brush,
  style: TextStyle,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = style.copy(
      brush = gradient
    ),
    modifier = modifier
  )
}
