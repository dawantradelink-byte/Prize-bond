package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BondMatch
import com.example.data.model.BondSummaryStats
import com.example.ui.components.AnimatedDrawCountdownCard
import com.example.ui.components.PortfolioSummaryBanner
import com.example.ui.theme.*

@Composable
fun MyBondsScreen(
  bonds: List<BondMatch>,
  stats: BondSummaryStats,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  filterWinnersOnly: Boolean,
  onFilterWinnersChange: (Boolean) -> Unit,
  onAddBondsClick: () -> Unit,
  onDeleteBond: (Long) -> Unit,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("my_bonds_list"),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Animated Upcoming Draw Countdown Clock
    item {
      AnimatedDrawCountdownCard(isDarkMode = isDarkMode)
    }

    // Summary Banner
    item {
      PortfolioSummaryBanner(
        stats = stats,
        isDarkMode = isDarkMode,
        onAddClick = onAddBondsClick
      )
    }

    // Search & Filter Row
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search by bond number or series...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = if (isDarkMode) CyberCyan else CyberViolet)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = if (isDarkMode) CyberCyan else InkPrimary)
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_bonds_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isDarkMode) CyberCyan else CyberViolet,
            unfocusedBorderColor = if (isDarkMode) Color(0x338B5CF6) else Color(0x4D06B6D4),
            focusedContainerColor = if (isDarkMode) Color(0x66090D18) else Color(0xB3FFFFFF),
            unfocusedContainerColor = if (isDarkMode) Color(0x66090D18) else Color(0x80FFFFFF)
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = !filterWinnersOnly,
              onClick = { onFilterWinnersChange(false) },
              label = { Text("All (${stats.totalBondsCount})", fontWeight = FontWeight.SemiBold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = if (isDarkMode) MotionSurfaceDarkElevated else MotionSurfaceLightElevated,
                selectedLabelColor = if (isDarkMode) Color.White else InkPrimary
              )
            )
            FilterChip(
              selected = filterWinnersOnly,
              onClick = { onFilterWinnersChange(true) },
              label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (filterWinnersOnly) RoyalGoldDark else CyberAmber
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Winners Only (${stats.winningBondsCount})", fontWeight = FontWeight.SemiBold)
                }
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = if (isDarkMode) CyberAmber else GoldLight,
                selectedLabelColor = RoyalGoldDark
              )
            )
          }

          if (bonds.isNotEmpty()) {
            Text(
              text = "${bonds.size} listed",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = if (isDarkMode) Color(0xFF94A3B8) else BodyMuted
            )
          }
        }
      }
    }

    // Empty State
    if (bonds.isEmpty()) {
      item {
        EmptyBondsView(
          isFilterActive = filterWinnersOnly || searchQuery.isNotEmpty(),
          isDarkMode = isDarkMode,
          onAddBondsClick = onAddBondsClick
        )
      }
    } else {
      // Bond Items
      items(
        items = bonds,
        key = { it.bond.id }
      ) { bondMatch ->
        BondCard(
          bondMatch = bondMatch,
          onDelete = { onDeleteBond(bondMatch.bond.id) },
          isDarkMode = isDarkMode
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

/**
 * Bond Card with Distinct Luxury Gold Highlighting for Winning Bonds!
 */
@Composable
fun BondCard(
  bondMatch: BondMatch,
  onDelete: () -> Unit,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  val isWinner = bondMatch.isWinner

  // DISTINCT COLOR HIGHLIGHTING FOR WINNERS
  val cardBackground = when {
    isWinner && !isDarkMode -> Brush.verticalGradient(
      listOf(
        Color(0xFFFFFBEB), // Radiant Luminous Gold Tint
        Color(0xFFFEF3C7),
        Color(0xFFFDE68A)
      )
    )
    isWinner && isDarkMode -> Brush.verticalGradient(
      listOf(
        Color(0xFF261D0C), // Deep Obsidian Gold
        Color(0xFF382A0F)
      )
    )
    isDarkMode -> Brush.linearGradient(
      listOf(GlassSurfaceDarkElevated, GlassSurfaceDark)
    )
    else -> Brush.linearGradient(
      listOf(GlassSurfaceLight, GlassSurfaceLightCard)
    )
  }

  val borderBrush = when {
    isWinner -> Brush.linearGradient(listOf(GoldPrimary, CyberAmber, CyberViolet, GoldLight))
    isDarkMode -> GlassBorderGradientDark
    else -> GlassBorderGradientLight
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(cardBackground)
      .border(
        width = if (isWinner) 1.5.dp else 1.dp,
        brush = borderBrush,
        shape = RoundedCornerShape(16.dp)
      )
      .padding(16.dp)
      .testTag("bond_card_${bondMatch.bond.bondNumber}")
  ) {
    Column {
      // Top Row: Bond Number, Series & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Series pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(
                if (isWinner) GoldPrimary else if (isDarkMode) Color(0xFF33312C) else SurfaceCreamStrong
              )
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = if (bondMatch.bond.series.isNotBlank()) bondMatch.bond.series else "All",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isWinner) RoyalGoldDark else if (isDarkMode) Color.White else InkPrimary
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          // 7-digit bond number
          Text(
            text = bondMatch.bond.bondNumber,
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp
            ),
            color = if (isWinner) (if (isDarkMode) GoldLight else RoyalGoldDark) else (if (isDarkMode) Color.White else InkPrimary)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isWinner) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(listOf(GoldPrimary, GoldAmber))
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.EmojiEvents,
                  contentDescription = null,
                  tint = RoyalGoldDark,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "WON ৳ ${bondMatch.totalPrizeWon}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                  ),
                  color = RoyalGoldDark
                )
              }
            }
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(36.dp)
              .testTag("delete_bond_${bondMatch.bond.bondNumber}")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Bond",
              tint = if (isDarkMode) Color(0xFFA09D96) else BodyMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // If WINNER: Display detailed win breakdown!
      if (isWinner) {
        Spacer(modifier = Modifier.height(10.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDarkMode) Color(0xFF1E1A11) else Color(0xFFFFF1C9))
            .padding(10.dp)
        ) {
          bondMatch.winningDraws.forEach { win ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = GoldDark,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${win.formattedPrize} • Draw #${win.drawNumber}",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                  ),
                  color = if (isDarkMode) GoldLight else RoyalGoldDark
                )
              }
              Text(
                text = win.drawDate,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Eligible for claim at Bangladesh Bank or scheduled commercial banks",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
          )
        }
      } else {
        // Non-winner status
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Checked against past 8 official draws • No prize yet",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
          )
          Text(
            text = "100 ৳",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isDarkMode) Color.White else InkPrimary
          )
        }
      }

      if (bondMatch.bond.note.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Note: ${bondMatch.bond.note}",
          style = MaterialTheme.typography.bodySmall,
          color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
        )
      }
    }
  }
}

@Composable
fun EmptyBondsView(
  isFilterActive: Boolean,
  isDarkMode: Boolean,
  onAddBondsClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 40.dp, horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = if (isFilterActive) "🔍" else "🎟️",
        fontSize = 48.sp
      )
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = if (isFilterActive) "No matching bonds found" else "No 100 Tk Bonds Added Yet",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold
        ),
        color = if (isDarkMode) Color.White else InkPrimary
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = if (isFilterActive)
          "Try changing your search query or reset the winner filter."
        else
          "Add your 100 Taka prize bond numbers manually, by series range, bulk text, or scan them using your camera with OCR.",
        style = MaterialTheme.typography.bodyMedium,
        color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted,
        modifier = Modifier.padding(horizontal = 20.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
      Spacer(modifier = Modifier.height(20.dp))

      if (!isFilterActive) {
        Button(
          onClick = onAddBondsClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = CoralPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("empty_state_add_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add or Scan Bonds")
        }
      }
    }
  }
}
