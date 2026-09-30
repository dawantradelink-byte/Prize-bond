package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RealtimeCameraScannerView
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuickCheckResult

@Composable
fun QuickCheckScreen(
  quickCheckResult: QuickCheckResult,
  onCheckNumber: (String) -> Unit,
  onClear: () -> Unit,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  var inputNumber by remember { mutableStateOf("") }
  var showCameraScanner by remember { mutableStateOf(false) }

  if (showCameraScanner) {
    Box(modifier = modifier.fillMaxSize()) {
      RealtimeCameraScannerView(
        onBondsScanned = { bonds ->
          val first = bonds.firstOrNull()
          if (first != null) {
            inputNumber = first.number
            onCheckNumber(first.number)
          }
          showCameraScanner = false
        },
        onDismiss = { showCameraScanner = false }
      )
    }
    return
  }

  val cardBg = if (isDarkMode) GlassSurfaceDarkElevated else GlassSurfaceLightCard
  val borderBrush = if (isDarkMode) GlassBorderGradientDark else GlassBorderGradientLight
  val borderCol = if (isDarkMode) Color(0x338B5CF6) else Color(0x4D06B6D4)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("quick_check_list"),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text(
          text = "Quick Prize Bond Checker",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
          ),
          color = if (isDarkMode) Color.White else InkPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Instant 1-click check of any 100 Taka bond number across all 8 active Bangladesh Bank quarterly draws without saving it to your portfolio.",
          style = MaterialTheme.typography.bodyMedium,
          color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
        )
      }
    }

    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(cardBg)
          .border(1.dp, borderBrush, RoundedCornerShape(14.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Enter 7-Digit Bond Number",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isDarkMode) Color.White else InkPrimary
          )

          OutlinedTextField(
            value = inputNumber,
            onValueChange = {
              if (it.length <= 7 && it.all { c -> c.isDigit() }) {
                inputNumber = it
              }
            },
            placeholder = { Text("e.g. 0428319 or 0876124") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (inputNumber.isNotEmpty()) {
                  IconButton(onClick = {
                    inputNumber = ""
                    onClear()
                  }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                  }
                }
                IconButton(onClick = { showCameraScanner = true }) {
                  Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Scan with Camera",
                    tint = CoralPrimary
                  )
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("quick_check_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = if (isDarkMode) CyberCyan else CyberViolet,
              unfocusedBorderColor = borderCol,
              focusedContainerColor = if (isDarkMode) Color(0xFF090D18) else Color.White,
              unfocusedContainerColor = if (isDarkMode) Color(0xFF090D18) else Color.White
            ),
            singleLine = true
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                if (inputNumber.isNotBlank()) {
                  onCheckNumber(inputNumber)
                }
              },
              enabled = inputNumber.isNotBlank(),
              modifier = Modifier
                .weight(1.3f)
                .height(48.dp)
                .testTag("quick_check_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isDarkMode) CyberViolet else CyberCyan,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Search, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Check Status")
            }

            Button(
              onClick = { showCameraScanner = true },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("quick_check_camera_scan_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = CoralPrimary,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Scan 📷")
            }
          }
        }
      }
    }

    // Results Display
    if (quickCheckResult.hasChecked) {
      item {
        val hasWon = quickCheckResult.matches.isNotEmpty()

        if (hasWon) {
          // WINNER CELEBRATION CARD
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF2C2513), Color(0xFF3A2E0F)))
                else Brush.verticalGradient(listOf(Color(0xFFFFF9E8), Color(0xFFFDECC6)))
              )
              .border(
                2.dp,
                Brush.linearGradient(listOf(GoldPrimary, GoldLight, GoldAmber)),
                RoundedCornerShape(16.dp)
              )
              .padding(20.dp)
              .testTag("quick_check_win_card")
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = RoyalGold,
                    modifier = Modifier.size(28.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "WINNER DETECTED!",
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontWeight = FontWeight.Bold
                    ),
                    color = if (isDarkMode) GoldLight else RoyalGoldDark
                  )
                }
              }

              Text(
                text = "Bond Number: ${quickCheckResult.queryNumber.padStart(7, '0')}",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 2.sp
                ),
                color = if (isDarkMode) Color.White else InkPrimary
              )

              quickCheckResult.matches.forEach { win ->
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkMode) Color(0xFF1E1A11) else Color(0xFFFFF1C9))
                    .padding(12.dp)
                ) {
                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = win.formattedPrize,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) GoldLight else RoyalGoldDark
                      )
                      Text(
                        text = "Draw #${win.drawNumber}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) Color.White else InkPrimary
                      )
                    }
                    Text(
                      text = "Date: ${win.drawDate}",
                      style = MaterialTheme.typography.bodySmall,
                      color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
                    )
                  }
                }
              }

              Text(
                text = "Under Bangladesh Bank rules, winning prize bonds can be encashed within 2 years from draw date at Bangladesh Bank counters or any scheduled commercial bank branch.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyRegular
              )
            }
          }
        } else {
          // NOT A WINNER CARD
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(cardBg)
              .border(1.dp, borderCol, RoundedCornerShape(14.dp))
              .padding(18.dp)
          ) {
            Row(verticalAlignment = Alignment.Top) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (isDarkMode) Color(0xFFA09D96) else BodyMuted,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "No Prize Found for ${quickCheckResult.queryNumber.padStart(7, '0')}",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (isDarkMode) Color.White else InkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "This number was not drawn in any of the active 8 quarterly draws (113th through 120th). Keep your bond safe, as each bond is automatically entered into future quarterly draws until redeemed.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}
