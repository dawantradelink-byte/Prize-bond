package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sync.NotificationHelper
import com.example.sync.WorkManagerScheduler
import com.example.ui.theme.*
import com.example.ui.viewmodel.UiOperationState

@Composable
fun SettingsScreen(
  isDarkMode: Boolean,
  onToggleDarkMode: () -> Unit,
  onSyncNow: () -> Unit,
  onClearAllBonds: () -> Unit,
  operationState: UiOperationState,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showClearConfirm by remember { mutableStateOf(false) }

  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
  }

  val cardBg = if (isDarkMode) GlassSurfaceDarkElevated else GlassSurfaceLightCard
  val borderCol = if (isDarkMode) Color(0x338B5CF6) else Color(0x4D06B6D4)

  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("Clear All Bonds?") },
      text = { Text("Are you sure you want to remove all saved prize bonds from your device? This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            onClearAllBonds()
            showClearConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text("Delete All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text(
          text = "Preferences & Information",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
          ),
          color = if (isDarkMode) Color.White else InkPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Manage dark theme, WorkManager background synchronization, alerts, and view Bangladesh Bank draw rules.",
          style = MaterialTheme.typography.bodyMedium,
          color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
        )
      }
    }

    // App Preferences Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(cardBg)
          .border(1.dp, borderCol, RoundedCornerShape(14.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(
            text = "Display & Theme",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isDarkMode) Color.White else InkPrimary
          )

          // Dark Mode Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDarkMode) Color.White else InkPrimary
              )
              Text(
                text = if (isDarkMode) "Deep obsidian mode active" else "Warm cream editorial mode active",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
              )
            }
            Switch(
              checked = isDarkMode,
              onCheckedChange = { onToggleDarkMode() },
              colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary),
              modifier = Modifier.testTag("settings_dark_mode_switch")
            )
          }

          // Notification Toggle / Permission
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Draw & Winning Notifications",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDarkMode) Color.White else InkPrimary
              )
              Text(
                text = if (hasNotificationPermission)
                  "Alerts active for new draw releases and winning bonds"
                else
                  "Permission required to receive draw result notifications",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
              )
            }
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              Button(
                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Enable", fontSize = 12.sp)
              }
            } else {
              Switch(
                checked = hasNotificationPermission,
                onCheckedChange = {
                  if (!it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
              )
            }
          }
        }
      }
    }

    // WorkManager Background Sync Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(cardBg)
          .border(1.dp, borderCol, RoundedCornerShape(14.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Sync, contentDescription = null, tint = CoralPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "WorkManager Background Sync",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = if (isDarkMode) Color.White else InkPrimary
            )
          }

          Text(
            text = "Background sync is scheduled via Android WorkManager every 12 hours. It connects to the Bangladesh Bank results source, checks for new quarterly draws, cross-references all your stored bonds, and posts a notification if a match is found.",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkMode) Color(0xFFA09D96) else BodyRegular
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onSyncNow,
              enabled = !operationState.isLoading,
              modifier = Modifier
                .weight(1f)
                .testTag("manual_sync_button"),
              colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
              shape = RoundedCornerShape(8.dp)
            ) {
              if (operationState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
              } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Now")
              }
            }

            OutlinedButton(
              onClick = {
                WorkManagerScheduler.triggerImmediateSync(context)
                NotificationHelper.sendDrawSyncNotification(
                  context,
                  120,
                  "31 October 2025"
                )
              },
              modifier = Modifier
                .weight(1f)
                .testTag("test_notification_button"),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Test Alert")
            }
          }
        }
      }
    }

    // Bangladesh Bank Prize Bond Guide & Rules Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(cardBg)
          .border(1.dp, borderCol, RoundedCornerShape(14.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Official Bangladesh Bank Rules",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isDarkMode) Color.White else InkPrimary
          )

          RuleRow(number = "1", title = "Quarterly Draws", desc = "Conducted every 3 months on 31 January, 30 April, 31 July, and 31 October.")
          RuleRow(number = "2", title = "2-Year Validity", desc = "Bonds are eligible to win for up to 2 years (past 8 draws) from the draw date.")
          RuleRow(number = "3", title = "Prize Schedule (per series)", desc = "1st: ৳ 6,00,000 (1x) • 2nd: ৳ 3,25,000 (1x) • 3rd: ৳ 1,00,000 (2x) • 4th: ৳ 50,000 (2x) • 5th: ৳ 10,000 (40x)")
          RuleRow(number = "4", title = "Tax on Winnings", desc = "As per National Board of Revenue (NBR) regulations, 20% source tax is deducted from all prize amounts at encashment.")
          RuleRow(number = "5", title = "Where to Claim", desc = "Claims can be submitted at any branch of Bangladesh Bank or authorized scheduled commercial banks with the original bond and NID.")
        }
      }
    }

    // Danger Zone (Clear All)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(cardBg)
          .border(1.dp, borderCol, RoundedCornerShape(14.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isDarkMode) Color.White else InkPrimary
          )
          Text(
            text = "All bond numbers are kept strictly private on your device in the Room database.",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
          )
          OutlinedButton(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("clear_all_bonds_button")
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear All Saved Bonds")
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
fun RuleRow(number: String, title: String, desc: String) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "$number. ",
      fontWeight = FontWeight.Bold,
      color = CoralPrimary
    )
    Column {
      Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
