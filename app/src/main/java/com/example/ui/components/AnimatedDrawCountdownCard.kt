package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.UpcomingDrawCalculator
import kotlinx.coroutines.delay

/**
 * MotionSites.ai Inspired Animated HUD Countdown Clock for Upcoming Bangladesh Bank Draw.
 * Features glowing neon borders, pulsing status orb, segmented cyber numerical blocks, and fluid motion.
 */
@Composable
fun AnimatedDrawCountdownCard(
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

  // Recalculate remaining time every second
  LaunchedEffect(Unit) {
    while (true) {
      delay(1000L)
      currentTimeMs = System.currentTimeMillis()
    }
  }

  val drawInfo = remember(currentTimeMs) {
    UpcomingDrawCalculator.getNextUpcomingDraw(currentTimeMs)
  }

  val countdown = remember(currentTimeMs, drawInfo.targetEpochMs) {
    UpcomingDrawCalculator.calculateRemainingTime(drawInfo.targetEpochMs, currentTimeMs)
  }

  // Pulsing live status animation
  val infiniteTransition = rememberInfiniteTransition(label = "motion_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  // Subtle animated sheen across the card border
  val borderShimmer by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(3500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "border_shimmer"
  )

  val cardBg = if (isDarkMode) {
    Brush.verticalGradient(
      listOf(
        GlassSurfaceDarkElevated,
        GlassSurfaceDark
      )
    )
  } else {
    Brush.verticalGradient(
      listOf(
        GlassSurfaceLight,
        GlassSurfaceLightCard
      )
    )
  }

  val borderBrush = if (isDarkMode) {
    Brush.linearGradient(
      colors = listOf(
        CyberViolet,
        CyberCyan,
        CyberAmber,
        CyberViolet
      ),
      start = androidx.compose.ui.geometry.Offset(0f, 0f),
      end = androidx.compose.ui.geometry.Offset(1000f * borderShimmer, 1000f)
    )
  } else {
    Brush.linearGradient(
      listOf(
        CyberViolet.copy(alpha = 0.6f),
        CyberCyan.copy(alpha = 0.6f),
        GoldAmber
      )
    )
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(cardBg)
      .border(1.5.dp, borderBrush, RoundedCornerShape(18.dp))
      .padding(18.dp)
      .testTag("upcoming_draw_countdown_card")
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      // Top Header: Badge, Live Pulse, Draw Number
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Glowing Neon Live Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDarkMode) Color(0x3306B6D4) else Color(0x1A06B6D4))
            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(CyberEmerald)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "LIVE COUNTDOWN",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontSize = 11.sp
              ),
              color = CyberEmerald
            )
          }
        }

        // Cybernetic Draw Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDarkMode) Color(0x338B5CF6) else Color(0x1A8B5CF6))
            .border(1.dp, CyberViolet.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = if (isDarkMode) CyberCyan else CyberViolet,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Draw #${drawInfo.drawNumber}",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = if (isDarkMode) Color.White else InkPrimary
            )
          }
        }
      }

      // Title & Subtitle with Motion Accent
      Column {
        Text(
          text = "Next 100 Tk Prize Bond Draw",
          style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          ),
          color = if (isDarkMode) Color.White else InkPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = if (isDarkMode) CyberCyan else CyberViolet,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "${drawInfo.drawDateFormatted} • 10:00 AM (${drawInfo.venue})",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (isDarkMode) Color(0xFFA5B4FC) else BodyMuted
          )
        }
      }

      // Animated Countdown Digit Boxes (Segmented Cyber HUD)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CountdownBlock(
          value = countdown.days.toString().padStart(2, '0'),
          label = "DAYS",
          accentColor = CyberViolet,
          isDarkMode = isDarkMode,
          modifier = Modifier.weight(1f)
        )
        CountdownBlock(
          value = countdown.hours.toString().padStart(2, '0'),
          label = "HOURS",
          accentColor = CyberCyan,
          isDarkMode = isDarkMode,
          modifier = Modifier.weight(1f)
        )
        CountdownBlock(
          value = countdown.minutes.toString().padStart(2, '0'),
          label = "MINS",
          accentColor = CyberAmber,
          isDarkMode = isDarkMode,
          modifier = Modifier.weight(1f)
        )
        CountdownBlock(
          value = countdown.seconds.toString().padStart(2, '0'),
          label = "SECS",
          accentColor = Color(0xFFFF5757),
          isDarkMode = isDarkMode,
          isPulsing = true,
          modifier = Modifier.weight(1f)
        )
      }

      // Bottom Note with High Tech Details
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "1st Prize: ৳ 6,00,000 • 46 prizes per series",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = if (isDarkMode) GoldLight else GoldDark
        )
        Text(
          text = "Bangladesh Bank",
          style = MaterialTheme.typography.labelSmall,
          color = if (isDarkMode) Color(0xFF94A3B8) else BodyMuted
        )
      }
    }
  }
}

/**
 * Segmented numerical HUD block with smooth number transition animations
 */
@Composable
fun CountdownBlock(
  value: String,
  label: String,
  accentColor: Color,
  isDarkMode: Boolean,
  isPulsing: Boolean = false,
  modifier: Modifier = Modifier
) {
  val blockBg = if (isDarkMode) {
    Color(0x66141B2D)
  } else {
    Color(0xB3FFFFFF)
  }

  val borderBrush = if (isPulsing) {
    Brush.linearGradient(listOf(accentColor, CyberViolet))
  } else {
    Brush.linearGradient(
      listOf(
        if (isDarkMode) MotionBorderDark else Hairline,
        if (isDarkMode) MotionBorderDark else Hairline
      )
    )
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(blockBg)
      .border(
        width = if (isPulsing) 1.5.dp else 1.dp,
        brush = borderBrush,
        shape = RoundedCornerShape(12.dp)
      )
      .padding(vertical = 10.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      AnimatedContent(
        targetState = value,
        transitionSpec = {
          if (targetState > initialState) {
            slideInVertically { height -> height } + fadeIn() togetherWith
                slideOutVertically { height -> -height } + fadeOut()
          } else {
            slideInVertically { height -> -height } + fadeIn() togetherWith
                slideOutVertically { height -> height } + fadeOut()
          }.using(
            // Disable clipping for smooth font rendering
            androidx.compose.animation.SizeTransform(clip = false)
          )
        },
        label = "countdown_$label"
      ) { targetValue ->
        Text(
          text = targetValue,
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
          ),
          color = if (isPulsing) accentColor else (if (isDarkMode) Color.White else InkPrimary)
        )
      }

      Spacer(modifier = Modifier.height(3.dp))

      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.1.sp,
          fontSize = 10.sp
        ),
        color = if (isPulsing) accentColor.copy(alpha = 0.9f) else (if (isDarkMode) Color(0xFF94A3B8) else BodyMuted)
      )
    }
  }
}
