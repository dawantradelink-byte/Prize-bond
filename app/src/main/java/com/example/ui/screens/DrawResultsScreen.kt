package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawResult
import com.example.data.model.WinningNumber
import com.example.ui.components.AnimatedDrawCountdownCard
import com.example.ui.theme.*

@Composable
fun DrawResultsScreen(
  draws: List<DrawResult>,
  selectedDraw: DrawResult?,
  selectedDrawNumbers: List<WinningNumber>,
  onSelectDraw: (DrawResult) -> Unit,
  onClearSelectedDraw: () -> Unit,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  val cardBg = if (isDarkMode) GlassSurfaceDarkElevated else GlassSurfaceLightCard
  val borderCol = if (isDarkMode) Color(0x338B5CF6) else Color(0x4D06B6D4)

  if (selectedDraw != null) {
    // Detail View of a specific Draw
    DrawDetailView(
      draw = selectedDraw,
      winningNumbers = selectedDrawNumbers,
      onBack = onClearSelectedDraw,
      isDarkMode = isDarkMode,
      modifier = modifier
    )
  } else {
    // List of All Active Quarterly Draws
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .testTag("draw_results_list"),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Upcoming Draw Animated Countdown
      item {
        AnimatedDrawCountdownCard(isDarkMode = isDarkMode)
      }

      item {
        Column {
          Text(
            text = "Bangladesh Bank Draw Results",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold
            ),
            color = if (isDarkMode) Color.White else InkPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Official 100 Taka prize bond draws remain valid for claim for 2 years (past 8 draws) as per Bangladesh Bank regulations.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
          )
        }
      }

      items(
        items = draws,
        key = { it.drawNumber }
      ) { draw ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(14.dp))
            .clickable { onSelectDraw(draw) }
            .padding(16.dp)
            .testTag("draw_item_${draw.drawNumber}")
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Draw #${draw.drawNumber}",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                  ),
                  color = if (isDarkMode) Color.White else InkPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Official Draw",
                  tint = EmeraldAccent,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Draw Date: ${draw.drawDate} • ${draw.drawPlace}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyRegular
              )
              Text(
                text = "Covers ${draw.seriesCount} series in circulation • Valid until ${draw.validUntil}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) GoldLight else GoldDark
              )
            }

            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "View Details",
              tint = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawDetailView(
  draw: DrawResult,
  winningNumbers: List<WinningNumber>,
  onBack: () -> Unit,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  val cardBg = if (isDarkMode) GlassSurfaceDarkElevated else GlassSurfaceLightCard
  val borderCol = if (isDarkMode) Color(0x338B5CF6) else Color(0x4D06B6D4)

  var selectedTierFilter by remember { mutableIntStateOf(0) } // 0: All, 1 to 5

  val filteredNumbers = remember(winningNumbers, selectedTierFilter) {
    if (selectedTierFilter == 0) winningNumbers else winningNumbers.filter { it.prizeTier == selectedTierFilter }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("back_from_draw_detail")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
          Text(
            text = "Draw #${draw.drawNumber} Results",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold
            ),
            color = if (isDarkMode) Color.White else InkPrimary
          )
          Text(
            text = "${draw.drawDate} • ${draw.drawPlace}",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
          )
        }
      }
    }

    // Prize Tier Tabs
    item {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val tiers = listOf(
          Pair(0, "All (${winningNumbers.size})"),
          Pair(1, "1st (৳ 6L)"),
          Pair(2, "2nd (৳ 3.25L)"),
          Pair(3, "3rd (৳ 1L)"),
          Pair(4, "4th (৳ 50k)"),
          Pair(5, "5th (৳ 10k)")
        )
        tiers.forEach { (tier, label) ->
          FilterChip(
            selected = selectedTierFilter == tier,
            onClick = { selectedTierFilter = tier },
            label = { Text(label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = if (isDarkMode) RoyalGoldDark else CoralPrimary,
              selectedLabelColor = Color.White
            )
          )
        }
      }
    }

    // Winning Numbers Grid / List
    items(
      items = filteredNumbers,
      key = { "${it.drawNumber}_${it.prizeTier}_${it.winningNumber}" }
    ) { win ->
      val isTopPrize = win.prizeTier in 1..2

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(if (isTopPrize) (if (isDarkMode) Color(0xFF2C2513) else Color(0xFFFFF9E8)) else cardBg)
          .border(
            width = if (isTopPrize) 1.5.dp else 1.dp,
            color = if (isTopPrize) GoldPrimary else borderCol,
            shape = RoundedCornerShape(12.dp)
          )
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = if (isTopPrize) GoldPrimary else (if (isDarkMode) Color(0xFFA09D96) else BodyMuted),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = win.formattedPrize,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isTopPrize) (if (isDarkMode) GoldLight else RoyalGoldDark) else (if (isDarkMode) Color.White else InkPrimary)
              )
              Text(
                text = "Applicable across all circulating series",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
              )
            }
          }

          Text(
            text = win.winningNumber,
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp
            ),
            color = if (isTopPrize) (if (isDarkMode) GoldLight else RoyalGoldDark) else (if (isDarkMode) Color.White else InkPrimary)
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}
