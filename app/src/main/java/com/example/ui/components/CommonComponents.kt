package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BondSummaryStats
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab

/**
 * MotionSites.ai Futuristic Glassmorphic Top Bar with Cyber Accent Highlights
 */
@Composable
fun AppTopHeader(
  isDarkMode: Boolean,
  onToggleDarkMode: () -> Unit,
  onSyncClick: () -> Unit,
  isSyncing: Boolean,
  modifier: Modifier = Modifier
) {
  val surfaceColor = if (isDarkMode) MotionSurfaceDark else MotionSurfaceLight

  // Animated spin for syncing state
  val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
  val syncRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = if (isDarkMode) GlassSurfaceDark else GlassSurfaceLight,
    tonalElevation = 0.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          brush = if (isDarkMode) GlassBorderGradientDark else GlassBorderGradientLight,
          shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        )
        .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Brand Mark & App Name
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.linearGradient(
                  listOf(CyberViolet, CyberCyan, GoldPrimary)
                )
              )
              .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = "PrizeBond Icon",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "PrizeBond BD",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontFamily = FontFamily.Serif,
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp
                ),
                color = if (isDarkMode) Color.White else InkPrimary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isDarkMode) Color(0x3306B6D4) else Color(0x1A06B6D4))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "AI OCR ⚡",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                  ),
                  color = if (isDarkMode) CyberCyan else CyberViolet
                )
              }
            }
            Text(
              text = "100 ৳ Bangladesh Bank Tracker",
              style = MaterialTheme.typography.bodySmall,
              color = if (isDarkMode) Color(0xFFA5B4FC) else BodyMuted
            )
          }
        }

        // Action Buttons (Sync & Dark Mode)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isDarkMode) MotionSurfaceDarkElevated else MotionSurfaceLightCard)
              .border(1.dp, if (isDarkMode) MotionBorderDark else Hairline, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            IconButton(
              onClick = onSyncClick,
              enabled = !isSyncing,
              modifier = Modifier
                .size(38.dp)
                .testTag("sync_header_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sync Results",
                tint = if (isSyncing) CyberAmber else (if (isDarkMode) CyberCyan else CyberViolet),
                modifier = if (isSyncing) Modifier.rotate(syncRotation) else Modifier
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isDarkMode) MotionSurfaceDarkElevated else MotionSurfaceLightCard)
              .border(1.dp, if (isDarkMode) MotionBorderDark else Hairline, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            IconButton(
              onClick = onToggleDarkMode,
              modifier = Modifier
                .size(38.dp)
                .testTag("dark_mode_toggle_button")
            ) {
              Icon(
                imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                contentDescription = "Toggle Dark Mode",
                tint = if (isDarkMode) CyberAmber else InkPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * MotionSites High-Tech Portfolio Summary Card
 */
@Composable
fun PortfolioSummaryBanner(
  stats: BondSummaryStats,
  isDarkMode: Boolean,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier
) {
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
  val borderBrush = if (isDarkMode) GlassBorderGradientDark else GlassBorderGradientLight

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(cardBg)
      .border(1.dp, borderBrush, RoundedCornerShape(18.dp))
      .padding(18.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isDarkMode) CyberCyan else CyberViolet)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "PORTFOLIO METRICS",
              style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              ),
              color = if (isDarkMode) CyberCyan else CyberViolet
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${stats.totalBondsCount} Bonds (৳ ${stats.totalInvestmentAmount})",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            ),
            color = if (isDarkMode) Color.White else InkPrimary
          )
        }

        // Winning badge with radiant cyber glow
        if (stats.winningBondsCount > 0) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.linearGradient(
                  listOf(GoldPrimary, CyberAmber)
                )
              )
              .border(1.dp, GoldLight, RoundedCornerShape(20.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = RoyalGoldDark,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${stats.winningBondsCount} WON! ৳ ${stats.totalPrizeAmountWon}",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold
                ),
                color = RoyalGoldDark
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Stats row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatItem(
          label = "Latest Draw",
          value = "#${stats.latestDrawNumber}",
          accent = CyberCyan,
          isDarkMode = isDarkMode,
          modifier = Modifier.weight(1f)
        )
        StatItem(
          label = "Valid Period",
          value = "8 Draws (2 Yrs)",
          accent = CyberViolet,
          isDarkMode = isDarkMode,
          modifier = Modifier.weight(1.3f)
        )
        StatItem(
          label = "Frequency",
          value = "Quarterly",
          accent = CyberEmerald,
          isDarkMode = isDarkMode,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun StatItem(
  label: String,
  value: String,
  accent: Color = CyberCyan,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isDarkMode) Color(0x66141B2D) else Color(0x99FFFFFF))
      .border(
        1.dp,
        if (isDarkMode) Color(0x338B5CF6) else Color(0x4D06B6D4),
        RoundedCornerShape(10.dp)
      )
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        ),
        color = if (isDarkMode) Color(0xFF94A3B8) else BodyMuted
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = if (isDarkMode) Color.White else InkPrimary
      )
    }
  }
}

/**
 * MotionSites Glassmorphic Navigation Bar with Luminous Indicators
 */
@Composable
fun AppBottomNav(
  currentTab: AppNavTab,
  onTabSelected: (AppNavTab) -> Unit,
  winnerCount: Int,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = if (isDarkMode) GlassSurfaceDark else GlassSurfaceLight,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isDarkMode) GlassBorderGradientDark else GlassBorderGradientLight
    ),
    tonalElevation = 0.dp
  ) {
    NavigationBar(
      modifier = Modifier.fillMaxWidth(),
      containerColor = Color.Transparent,
      contentColor = if (isDarkMode) Color.White else InkPrimary,
      tonalElevation = 0.dp
    ) {
    val selectedColor = if (isDarkMode) CyberCyan else CyberViolet
    val indicatorBg = if (isDarkMode) Color(0x338B5CF6) else Color(0x268B5CF6)

    NavigationBarItem(
      selected = currentTab == AppNavTab.MY_BONDS,
      onClick = { onTabSelected(AppNavTab.MY_BONDS) },
      icon = {
        Box {
          Icon(
            imageVector = Icons.Default.ConfirmationNumber,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
          )
          if (winnerCount > 0) {
            Badge(
              containerColor = CyberAmber,
              contentColor = RoyalGoldDark,
              modifier = Modifier.align(Alignment.TopEnd)
            ) {
              Text(
                text = "$winnerCount",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }
      },
      label = { Text("My Bonds", fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedColor,
        selectedTextColor = selectedColor,
        indicatorColor = indicatorBg,
        unselectedIconColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted,
        unselectedTextColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted
      ),
      modifier = Modifier.testTag("nav_my_bonds")
    )

    NavigationBarItem(
      selected = currentTab == AppNavTab.ADD_BONDS,
      onClick = { onTabSelected(AppNavTab.ADD_BONDS) },
      icon = {
        Icon(
          imageVector = Icons.Default.AddCircle,
          contentDescription = null,
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Add", fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedColor,
        selectedTextColor = selectedColor,
        indicatorColor = indicatorBg,
        unselectedIconColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted,
        unselectedTextColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted
      ),
      modifier = Modifier.testTag("nav_add_bonds")
    )

    NavigationBarItem(
      selected = currentTab == AppNavTab.DRAW_RESULTS,
      onClick = { onTabSelected(AppNavTab.DRAW_RESULTS) },
      icon = {
        Icon(
          imageVector = Icons.Default.ReceiptLong,
          contentDescription = null,
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Draws", fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedColor,
        selectedTextColor = selectedColor,
        indicatorColor = indicatorBg,
        unselectedIconColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted,
        unselectedTextColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted
      ),
      modifier = Modifier.testTag("nav_draw_results")
    )

    NavigationBarItem(
      selected = currentTab == AppNavTab.QUICK_CHECK,
      onClick = { onTabSelected(AppNavTab.QUICK_CHECK) },
      icon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Quick Check", fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedColor,
        selectedTextColor = selectedColor,
        indicatorColor = indicatorBg,
        unselectedIconColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted,
        unselectedTextColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted
      ),
      modifier = Modifier.testTag("nav_quick_check")
    )

    NavigationBarItem(
      selected = currentTab == AppNavTab.SETTINGS,
      onClick = { onTabSelected(AppNavTab.SETTINGS) },
      icon = {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = null,
          modifier = Modifier.size(22.dp)
        )
      },
      label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedColor,
        selectedTextColor = selectedColor,
        indicatorColor = indicatorBg,
        unselectedIconColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted,
        unselectedTextColor = if (isDarkMode) Color(0xFF64748B) else BodyMuted
      ),
      modifier = Modifier.testTag("nav_settings")
    )
  }
}
}
