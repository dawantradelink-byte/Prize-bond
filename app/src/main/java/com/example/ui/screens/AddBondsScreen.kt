package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ocr.ScannedBondCandidate
import com.example.ui.components.RealtimeCameraScannerView
import com.example.ui.theme.*
import com.example.ui.viewmodel.UiOperationState

val POPULAR_BANGLADESH_SERIES = listOf(
  "All", "কখ", "খগ", "গঘ", "ঘঙ", "ঙচ", "চছ", "ছজ", "জঝ", "ঝঞ", "টঠ", "ঠড", "ঢণ", "তথ", "দধ", "নপ"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBondsScreen(
  onAddSingle: (String, String, String) -> Unit,
  onAddRange: (String, String, String) -> Unit,
  onAddBulk: (String, String) -> Unit,
  onScanBitmap: (Bitmap) -> Unit,
  onScanUri: (Context, Uri) -> Unit,
  scannedCandidates: List<ScannedBondCandidate>,
  isOcrScanning: Boolean,
  onToggleCandidate: (String) -> Unit,
  onRemoveCandidate: (String) -> Unit,
  onImportScannedBonds: () -> Unit,
  onClearScanned: () -> Unit,
  onAddRealtimeBonds: (List<ScannedBondCandidate>) -> Unit = {},
  operationState: UiOperationState,
  isDarkMode: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showRealtimeScanner by remember { mutableStateOf(false) }
  var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Single, 1: Range, 2: Bulk, 3: OCR Scan

  if (showRealtimeScanner) {
    Box(modifier = modifier.fillMaxSize()) {
      RealtimeCameraScannerView(
        onBondsScanned = { bonds ->
          onAddRealtimeBonds(bonds)
          showRealtimeScanner = false
        },
        onDismiss = { showRealtimeScanner = false }
      )
    }
    return
  }

  // Single Bond Form States
  var singleNumber by remember { mutableStateOf("") }
  var singleSeries by remember { mutableStateOf("All") }
  var singleNote by remember { mutableStateOf("") }

  // Range Form States
  var rangeStartNumber by remember { mutableStateOf("") }
  var rangeEndNumber by remember { mutableStateOf("") }
  var rangeSeries by remember { mutableStateOf("All") }

  // Bulk Form States
  var bulkText by remember { mutableStateOf("") }
  var bulkSeries by remember { mutableStateOf("All") }

  // Camera & Photo Picker Launchers
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let { onScanUri(context, it) }
  }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    bitmap?.let { onScanBitmap(it) }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      cameraLauncher.launch(null)
    }
  }

  val cardBg = if (isDarkMode) GlassSurfaceDarkElevated else GlassSurfaceLightCard
  val borderCol = if (isDarkMode) Color(0x338B5CF6) else Color(0x4D06B6D4)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("add_bonds_list"),
    contentPadding = PaddingValues(vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Info
    item {
      Column {
        Text(
          text = "Enter 100 Taka Prize Bonds",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
          ),
          color = if (isDarkMode) Color.White else InkPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Add bonds manually, in ranges, bulk text, or scan certificates using ML Kit OCR.",
          style = MaterialTheme.typography.bodyMedium,
          color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
        )
      }
    }

    // Sub Tab Selector (Scrollable to comfortably fit OCR tab)
    item {
      ScrollableTabRow(
        selectedTabIndex = selectedSubTab,
        containerColor = cardBg,
        contentColor = if (isDarkMode) CyberCyan else CyberViolet,
        edgePadding = 8.dp,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
            color = if (isDarkMode) CyberCyan else CyberViolet
          )
        },
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .border(1.dp, borderCol, RoundedCornerShape(10.dp))
      ) {
        Tab(
          selected = selectedSubTab == 0,
          onClick = { selectedSubTab = 0 },
          text = { Text("Single", fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.testTag("tab_single_bond")
        )
        Tab(
          selected = selectedSubTab == 1,
          onClick = { selectedSubTab = 1 },
          text = { Text("Series Range", fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.testTag("tab_series_range")
        )
        Tab(
          selected = selectedSubTab == 2,
          onClick = { selectedSubTab = 2 },
          text = { Text("Bulk Paste", fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.testTag("tab_bulk_paste")
        )
        Tab(
          selected = selectedSubTab == 3,
          onClick = { selectedSubTab = 3 },
          text = { Text("OCR Scan 📷", fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.testTag("tab_ocr_scan")
        )
      }
    }

    // Content based on selected tab
    when (selectedSubTab) {
      0 -> {
        // --- SINGLE BOND INPUT ---
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(cardBg)
              .border(1.dp, borderCol, RoundedCornerShape(14.dp))
              .padding(18.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Text(
                text = "Bond Number (7 Digits)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) Color.White else InkPrimary
              )

              OutlinedTextField(
                value = singleNumber,
                onValueChange = { if (it.length <= 7 && it.all { c -> c.isDigit() }) singleNumber = it },
                placeholder = { Text("e.g. 0123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_single_bond_number"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralPrimary,
                  unfocusedBorderColor = borderCol
                ),
                singleLine = true
              )

              // Series Selection
              Text(
                text = "Bond Series (optional / all series by default)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDarkMode) Color.White else InkPrimary
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                POPULAR_BANGLADESH_SERIES.forEach { series ->
                  val isSelected = singleSeries == series
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        if (isSelected) CoralPrimary else if (isDarkMode) Color(0xFF33312C) else SurfaceCreamStrong
                      )
                      .clickable { singleSeries = series }
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = series,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (isSelected) Color.White else (if (isDarkMode) Color.White else InkPrimary)
                    )
                  }
                }
              }

              OutlinedTextField(
                value = singleNote,
                onValueChange = { singleNote = it },
                placeholder = { Text("Note (e.g. Purchased from Sonali Bank)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralPrimary,
                  unfocusedBorderColor = borderCol
                ),
                singleLine = true
              )

              Button(
                onClick = {
                  if (singleNumber.isNotBlank()) {
                    onAddSingle(singleNumber, singleSeries, singleNote)
                    singleNumber = ""
                    singleNote = ""
                  }
                },
                enabled = singleNumber.length in 1..7 && !operationState.isLoading,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("submit_single_bond_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isDarkMode) CyberViolet else CyberCyan,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                if (operationState.isLoading) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                  Icon(Icons.Default.Add, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Save 100 Tk Bond")
                }
              }
            }
          }
        }
      }

      1 -> {
        // --- SERIES RANGE INPUT ---
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(cardBg)
              .border(1.dp, borderCol, RoundedCornerShape(14.dp))
              .padding(18.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Text(
                text = "Add Bond Book / Series Range",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) Color.White else InkPrimary
              )
              Text(
                text = "Ideal for books of 10, 20, 50, or 100 bonds purchased consecutively.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedTextField(
                  value = rangeStartNumber,
                  onValueChange = { if (it.length <= 7 && it.all { c -> c.isDigit() }) rangeStartNumber = it },
                  label = { Text("Start Number") },
                  placeholder = { Text("0123400") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("input_range_start"),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = borderCol
                  ),
                  singleLine = true
                )

                OutlinedTextField(
                  value = rangeEndNumber,
                  onValueChange = { if (it.length <= 7 && it.all { c -> c.isDigit() }) rangeEndNumber = it },
                  label = { Text("End Number") },
                  placeholder = { Text("0123450") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("input_range_end"),
                  shape = RoundedCornerShape(10.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = borderCol
                  ),
                  singleLine = true
                )
              }

              Text(
                text = "Series",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDarkMode) Color.White else InkPrimary
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                POPULAR_BANGLADESH_SERIES.forEach { series ->
                  val isSelected = rangeSeries == series
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        if (isSelected) CoralPrimary else if (isDarkMode) Color(0xFF33312C) else SurfaceCreamStrong
                      )
                      .clickable { rangeSeries = series }
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = series,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (isSelected) Color.White else (if (isDarkMode) Color.White else InkPrimary)
                    )
                  }
                }
              }

              Button(
                onClick = {
                  onAddRange(rangeStartNumber, rangeEndNumber, rangeSeries)
                  rangeStartNumber = ""
                  rangeEndNumber = ""
                },
                enabled = rangeStartNumber.isNotBlank() && rangeEndNumber.isNotBlank() && !operationState.isLoading,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("submit_range_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isDarkMode) CyberViolet else CyberCyan,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                if (operationState.isLoading) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                  Icon(Icons.Default.SwapHoriz, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Generate & Save Range")
                }
              }
            }
          }
        }
      }

      2 -> {
        // --- BULK PASTE INPUT ---
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(cardBg)
              .border(1.dp, borderCol, RoundedCornerShape(14.dp))
              .padding(18.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Text(
                text = "Bulk Paste Numbers",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) Color.White else InkPrimary
              )
              Text(
                text = "Paste a list of bond numbers separated by commas, spaces, or newlines.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyMuted
              )

              OutlinedTextField(
                value = bulkText,
                onValueChange = { bulkText = it },
                placeholder = { Text("0123456, 0234567, 0345678\n0456789\n0567890") },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
                  .testTag("input_bulk_text"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CoralPrimary,
                  unfocusedBorderColor = borderCol
                )
              )

              Text(
                text = "Default Series for this batch",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isDarkMode) Color.White else InkPrimary
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                POPULAR_BANGLADESH_SERIES.take(10).forEach { series ->
                  val isSelected = bulkSeries == series
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        if (isSelected) CoralPrimary else if (isDarkMode) Color(0xFF33312C) else SurfaceCreamStrong
                      )
                      .clickable { bulkSeries = series }
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = series,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (isSelected) Color.White else (if (isDarkMode) Color.White else InkPrimary)
                    )
                  }
                }
              }

              Button(
                onClick = {
                  onAddBulk(bulkText, bulkSeries)
                  bulkText = ""
                },
                enabled = bulkText.isNotBlank() && !operationState.isLoading,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("submit_bulk_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isDarkMode) CyberViolet else CyberCyan,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                if (operationState.isLoading) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                  Icon(Icons.Default.ContentPaste, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Import Bond Numbers")
                }
              }
            }
          }
        }
      }

      3 -> {
        // --- OCR SCANNER (CAMERA & PHOTO PICKER WITH ML KIT) ---
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(cardBg)
              .border(1.dp, borderCol, RoundedCornerShape(14.dp))
              .padding(18.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.DocumentScanner,
                  contentDescription = null,
                  tint = CoralPrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Scan Bonds via ML Kit OCR",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (isDarkMode) Color.White else InkPrimary
                )
              }

              Text(
                text = "Point your camera at a 100 Taka prize bond certificate or select a photo from your gallery. Google ML Kit will automatically recognize 7-digit numbers (Latin and Bengali digits) and circulating series letters in real-time.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFA09D96) else BodyRegular
              )

              // Real-Time Camera Scanner Primary Action Button
              Button(
                onClick = { showRealtimeScanner = true },
                enabled = !isOcrScanning,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("ocr_live_camera_scan_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = CoralPrimary,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Start Live Camera Scanner ⚡",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
              }

              // Fallback Photo & Gallery Buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                      cameraLauncher.launch(null)
                    } else {
                      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                  },
                  enabled = !isOcrScanning,
                  modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("ocr_camera_scan_button"),
                  colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDarkMode) Color.White else InkPrimary
                  ),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Take Photo")
                }

                OutlinedButton(
                  onClick = {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  },
                  enabled = !isOcrScanning,
                  modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("ocr_gallery_pick_button"),
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDarkMode) GoldLight else RoyalGoldDark
                  )
                ) {
                  Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("From Gallery")
                }
              }

              if (isOcrScanning) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = CoralPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                      text = "Analyzing image with Google ML Kit...",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                      color = if (isDarkMode) Color.White else InkPrimary
                    )
                  }
                }
              }
            }
          }
        }

        // Display Scanned Candidates List
        if (scannedCandidates.isNotEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDarkMode) Color(0xFF221F18) else GoldSuperLight)
                .border(1.5.dp, GoldAmber, RoundedCornerShape(14.dp))
                .padding(16.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Scanned Bonds (${scannedCandidates.count { it.isSelected }} of ${scannedCandidates.size} selected)",
                      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (isDarkMode) GoldLight else RoyalGoldDark
                    )
                    Text(
                      text = "Review detected bond numbers before adding to Room database:",
                      style = MaterialTheme.typography.bodySmall,
                      color = if (isDarkMode) Color(0xFFA09D96) else BodyRegular
                    )
                  }
                  IconButton(onClick = onClearScanned) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear scanned list")
                  }
                }

                scannedCandidates.forEach { candidate ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isDarkMode) Color(0xFF2C271E) else Color.White)
                      .clickable { onToggleCandidate(candidate.id) }
                      .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Checkbox(
                        checked = candidate.isSelected,
                        onCheckedChange = { onToggleCandidate(candidate.id) },
                        colors = CheckboxDefaults.colors(checkedColor = CoralPrimary)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(CoralPrimary)
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = candidate.series,
                          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                          color = Color.White
                        )
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = candidate.number,
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontFamily = FontFamily.Monospace,
                          fontWeight = FontWeight.Bold,
                          letterSpacing = 1.sp
                        ),
                        color = if (isDarkMode) Color.White else InkPrimary
                      )
                    }

                    IconButton(onClick = { onRemoveCandidate(candidate.id) }) {
                      Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = if (isDarkMode) Color(0xFFA09D96) else BodyMuted,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }

                Button(
                  onClick = onImportScannedBonds,
                  enabled = scannedCandidates.any { it.isSelected },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("import_scanned_bonds_button"),
                  colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Check, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Save ${scannedCandidates.count { it.isSelected }} Scanned Bond(s) to Portfolio")
                }
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
