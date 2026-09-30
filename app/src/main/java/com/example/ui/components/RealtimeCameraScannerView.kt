package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ocr.ScannedBondCandidate
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.MotionCanvasDark
import com.example.ui.theme.MotionSurfaceDarkElevated
import com.example.util.CameraHelper

/**
 * Real-time camera viewfinder composable using CameraX and ML Kit text recognition
 * to scan 100 Taka Bangladesh prize bonds with a futuristic cyber HUD design.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealtimeCameraScannerView(
  onBondsScanned: (List<ScannedBondCandidate>) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
    if (!isGranted) {
      onDismiss()
    }
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  if (!hasCameraPermission) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(MotionCanvasDark),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.DocumentScanner,
          contentDescription = null,
          tint = CyberCyan,
          modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Camera Permission Required",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "To scan physical prize bonds using ML Kit, please grant camera access.",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFA09D96)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
          colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Grant Permission")
        }
      }
    }
    return
  }

  val detectedBonds = remember { mutableStateListOf<ScannedBondCandidate>() }
  var isTorchActive by remember { mutableStateOf(false) }
  var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

  val cameraHelper = remember {
    CameraHelper(
      onBondsDetected = { newCandidates, _ ->
        val currentNumbers = detectedBonds.map { it.number }.toSet()
        val uniqueNew = newCandidates.filter { it.number !in currentNumbers }
        if (uniqueNew.isNotEmpty()) {
          detectedBonds.addAll(uniqueNew)
        }
      }
    )
  }

  DisposableEffect(lifecycleOwner) {
    onDispose {
      cameraHelper.release()
    }
  }

  // Animation for laser scan line
  val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
  val laserProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_y"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // 1. CameraX PreviewView
    AndroidView(
      factory = { ctx ->
        PreviewView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          scaleType = PreviewView.ScaleType.FILL_CENTER
          implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }.also { view ->
          previewViewInstance = view
          cameraHelper.startCamera(ctx, lifecycleOwner, view)
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // 2. Viewfinder & Cyber HUD Overlay
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      // Viewfinder target rect (sized for 100 Taka prize bond certificate)
      val boxWidth = canvasWidth * 0.88f
      val boxHeight = boxWidth * 0.55f
      val left = (canvasWidth - boxWidth) / 2f
      val top = (canvasHeight - boxHeight) / 2.6f

      // Dark translucent backdrop outside the target area
      // Top rect
      drawRect(
        color = Color(0x99080A10),
        topLeft = Offset(0f, 0f),
        size = Size(canvasWidth, top)
      )
      // Bottom rect
      drawRect(
        color = Color(0x99080A10),
        topLeft = Offset(0f, top + boxHeight),
        size = Size(canvasWidth, canvasHeight - (top + boxHeight))
      )
      // Left rect
      drawRect(
        color = Color(0x99080A10),
        topLeft = Offset(0f, top),
        size = Size(left, boxHeight)
      )
      // Right rect
      drawRect(
        color = Color(0x99080A10),
        topLeft = Offset(left + boxWidth, top),
        size = Size(canvasWidth - (left + boxWidth), boxHeight)
      )

      // Target bounding border
      drawRoundRect(
        color = Color(0x4D06B6D4),
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 2f)
      )

      // Futuristic corner brackets
      val cornerLength = 32f
      val cornerStroke = 6f
      val cornerColor = Color(0xFF06B6D4) // Cyber Cyan

      // Top-Left corner
      drawLine(cornerColor, Offset(left, top + cornerLength), Offset(left, top), cornerStroke)
      drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), cornerStroke)

      // Top-Right corner
      drawLine(cornerColor, Offset(left + boxWidth - cornerLength, top), Offset(left + boxWidth, top), cornerStroke)
      drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLength), cornerStroke)

      // Bottom-Left corner
      drawLine(cornerColor, Offset(left, top + boxHeight - cornerLength), Offset(left, top + boxHeight), cornerStroke)
      drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left + cornerLength, top + boxHeight), cornerStroke)

      // Bottom-Right corner
      drawLine(cornerColor, Offset(left + boxWidth - cornerLength, top + boxHeight), Offset(left + boxWidth, top + boxHeight), cornerStroke)
      drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight - cornerLength), Offset(left + boxWidth, top + boxHeight), cornerStroke)

      // Animated glowing laser scanner line
      val laserY = top + (boxHeight * laserProgress)
      drawLine(
        brush = Brush.horizontalGradient(
          colors = listOf(
            Color.Transparent,
            Color(0xFFFF5757),
            Color(0xFF06B6D4),
            Color(0xFFFF5757),
            Color.Transparent
          )
        ),
        start = Offset(left + 8f, laserY),
        end = Offset(left + boxWidth - 8f, laserY),
        strokeWidth = 4f
      )
    }

    // 3. Top Controls Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onDismiss,
        modifier = Modifier
          .clip(CircleShape)
          .background(Color(0x99141B2D))
          .size(44.dp)
      ) {
        Icon(Icons.Default.Close, contentDescription = "Close scanner", tint = Color.White)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Torch button
        IconButton(
          onClick = {
            isTorchActive = !isTorchActive
            cameraHelper.toggleTorch(isTorchActive)
          },
          modifier = Modifier
            .clip(CircleShape)
            .background(if (isTorchActive) GoldAmber else Color(0x99141B2D))
            .size(44.dp)
        ) {
          Icon(
            imageVector = if (isTorchActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
            contentDescription = "Toggle flashlight",
            tint = Color.White
          )
        }

        // Camera flip button
        IconButton(
          onClick = {
            previewViewInstance?.let { view ->
              cameraHelper.switchCamera(lifecycleOwner, view)
            }
          },
          modifier = Modifier
            .clip(CircleShape)
            .background(Color(0x99141B2D))
            .size(44.dp)
        ) {
          Icon(Icons.Default.Cameraswitch, contentDescription = "Switch camera", tint = Color.White)
        }
      }
    }

    // 4. Center Guidance HUD Tag
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(bottom = 180.dp)
    ) {
      Surface(
        color = Color(0xCC0E131F),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D06B6D4))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(0xFF10B981))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Align 100 Taka Prize Bond in Frame",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )
        }
      }
    }

    // 5. Bottom Tray: Live Detected Bonds List and Action Button
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        .background(Color(0xE60E131F))
        .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Real-Time ML Kit Scanner",
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold
            ),
            color = Color.White
          )
          Text(
            text = "${detectedBonds.size} bond number(s) identified",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFA09D96)
          )
        }

        if (detectedBonds.isNotEmpty()) {
          Text(
            text = "Clear",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFFF5757),
            modifier = Modifier
              .clickable { detectedBonds.clear() }
              .padding(4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Chips of detected numbers
      if (detectedBonds.isNotEmpty()) {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          detectedBonds.take(8).forEach { candidate ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141B2D))
                .border(1.dp, Color(0x6606B6D4), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${candidate.series} ",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFFFF5757)
                )
                Text(
                  text = candidate.number,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  ),
                  color = Color.White
                )
              }
            }
          }
          if (detectedBonds.size > 8) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141B2D))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "+${detectedBonds.size - 8} more",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFA09D96)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Finish / Import Button
      Button(
        onClick = {
          onBondsScanned(detectedBonds.toList())
          onDismiss()
        },
        enabled = detectedBonds.isNotEmpty(),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("confirm_realtime_scanned_bonds"),
        colors = ButtonDefaults.buttonColors(
          containerColor = CoralPrimary,
          disabledContainerColor = Color(0xFF232D47),
          disabledContentColor = Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (detectedBonds.isNotEmpty()) {
            "Add ${detectedBonds.size} Scanned Bond(s) to List"
          } else {
            "Scanning for 7-digit bond numbers..."
          },
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
