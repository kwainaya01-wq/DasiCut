package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.TextAnimation
import com.example.model.TextOverlay
import com.example.ui.theme.*

@Composable
fun TextEditorDialog(
  initialOverlay: TextOverlay? = null,
  initialText: String = "",
  onDismiss: () -> Unit,
  onSaveOverlay: (
    text: String,
    colorHex: String,
    bgColorHex: String?,
    fontStyle: String,
    animation: TextAnimation,
    fontSizeSp: Float,
    hasStroke: Boolean,
    strokeColorHex: String,
    hasShadow: Boolean,
    shadowColorHex: String,
    alignment: String,
    rotation: Float,
    scale: Float,
    durationMs: Long
  ) -> Unit
) {
  var text by remember { mutableStateOf(initialOverlay?.text ?: initialText) }
  var selectedColorHex by remember { mutableStateOf(initialOverlay?.colorHex ?: "#FFFFFF") }
  var selectedBgHex by remember { mutableStateOf<String?>(initialOverlay?.bgColorHex ?: "#99000000") }
  var fontStyleName by remember { mutableStateOf(initialOverlay?.fontStyleName ?: "Outfit Bold") }
  var selectedAnimation by remember { mutableStateOf(initialOverlay?.animationType ?: TextAnimation.POP) }
  var fontSizeSp by remember { mutableFloatStateOf(initialOverlay?.fontSizeSp ?: 26f) }
  var hasStroke by remember { mutableStateOf(initialOverlay?.hasStroke ?: true) }
  var strokeColorHex by remember { mutableStateOf(initialOverlay?.strokeColorHex ?: "#000000") }
  var hasShadow by remember { mutableStateOf(initialOverlay?.hasShadow ?: false) }
  var shadowColorHex by remember { mutableStateOf(initialOverlay?.shadowColorHex ?: "#000000") }
  var alignment by remember { mutableStateOf(initialOverlay?.alignment ?: "CENTER") }
  var rotationDegrees by remember { mutableFloatStateOf(initialOverlay?.rotationDegrees ?: 0f) }
  var scale by remember { mutableFloatStateOf(initialOverlay?.scale ?: 1.0f) }
  var durationMs by remember { mutableLongStateOf(initialOverlay?.durationMs ?: 3000L) }

  val colorPresets = listOf(
    "#FFFFFF" to "White",
    "#FFD54F" to "Gold",
    "#00E676" to "Emerald",
    "#FF2A85" to "Neon Pink",
    "#00E5FF" to "Cyan",
    "#FF6D00" to "Saffron",
    "#FF334B" to "Red",
    "#9C27B0" to "Purple"
  )

  val bgPresets = listOf(
    null to "None",
    "#99000000" to "Dark Pill",
    "#9900E676" to "Emerald Pill",
    "#99FFD54F" to "Gold Pill",
    "#99FF2A85" to "Pink Pill",
    "#991E3A5F" to "Blue Pill"
  )

  val fonts = listOf(
    "Outfit Bold", "Bebas Neue", "Montserrat", "Cinzel Cinematic", "Orbitron Cyber", "Caveat Script"
  )

  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CharcoalSurface,
      border = BorderStroke(1.dp, CharcoalBorder),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Text & Typography ✍️", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Text Preview Card
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color.Black,
          modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
          border = BorderStroke(1.dp, CharcoalBorder)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(android.graphics.Color.parseColor(selectedBgHex ?: "#00000000")),
              border = if (hasStroke) BorderStroke(1.dp, Color(android.graphics.Color.parseColor(strokeColorHex))) else null,
              shadowElevation = if (hasShadow) 8.dp else 0.dp,
              modifier = Modifier.padding(8.dp)
            ) {
              Text(
                text = if (text.isNotBlank()) text else "Your Title Here",
                color = Color(android.graphics.Color.parseColor(selectedColorHex)),
                fontSize = (fontSizeSp * 0.7f * scale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = when (alignment.uppercase()) {
                  "LEFT" -> TextAlign.Left
                  "RIGHT" -> TextAlign.Right
                  else -> TextAlign.Center
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable settings body
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(scrollState)
        ) {
          // Input field
          OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Enter title or caption...", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldPrimary,
              unfocusedBorderColor = CharcoalBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth().testTag("text_input_field")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Alignment Selector
          Text("Alignment", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("LEFT" to Icons.Default.FormatAlignLeft, "CENTER" to Icons.Default.FormatAlignCenter, "RIGHT" to Icons.Default.FormatAlignRight).forEach { (alignKey, icon) ->
              FilterChip(
                selected = alignment == alignKey,
                onClick = { alignment = alignKey },
                label = { Icon(icon, contentDescription = alignKey, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = CharcoalBackground
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Font Family
          Text("Font Family", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            fonts.forEach { fName ->
              FilterChip(
                selected = fontStyleName == fName,
                onClick = { fontStyleName = fName },
                label = { Text(fName, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CharcoalCardSelected,
                  selectedLabelColor = EmeraldPrimary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Font Size Slider
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Font Size", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("${fontSizeSp.toInt()} sp", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
          }
          Slider(
            value = fontSizeSp,
            onValueChange = { fontSizeSp = it },
            valueRange = 14f..52f,
            colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
          )

          // Rotation Slider
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Rotation", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("${rotationDegrees.toInt()}°", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
          }
          Slider(
            value = rotationDegrees,
            onValueChange = { rotationDegrees = it },
            valueRange = -180f..180f,
            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
          )

          // Scale Slider
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Scale", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("%.2fx".format(scale), style = MaterialTheme.typography.labelSmall, color = GoldAccent)
          }
          Slider(
            value = scale,
            onValueChange = { scale = it },
            valueRange = 0.5f..2.5f,
            colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
          )

          // Duration Slider
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Display Duration", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("%.1f s".format(durationMs / 1000f), style = MaterialTheme.typography.labelSmall, color = NeonPink)
          }
          Slider(
            value = durationMs.toFloat(),
            onValueChange = { durationMs = it.toLong() },
            valueRange = 500f..10000f,
            colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Text Color swatches
          Text("Text Color", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            colorPresets.forEach { (hex, _) ->
              val col = Color(android.graphics.Color.parseColor(hex))
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .background(col, CircleShape)
                  .border(if (selectedColorHex == hex) 2.dp else 1.dp, if (selectedColorHex == hex) EmeraldPrimary else CharcoalBorder, CircleShape)
                  .clickable { selectedColorHex = hex }
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Outline & Shadow Toggles
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            FilterChip(
              selected = hasStroke,
              onClick = { hasStroke = !hasStroke },
              label = { Text("Outline Border") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = CharcoalBackground
              )
            )
            FilterChip(
              selected = hasShadow,
              onClick = { hasShadow = !hasShadow },
              label = { Text("Drop Shadow") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GoldAccent,
                selectedLabelColor = CharcoalBackground
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Background pill swatches
          Text("Background Pill", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            bgPresets.forEach { (hex, label) ->
              FilterChip(
                selected = selectedBgHex == hex,
                onClick = { selectedBgHex = hex },
                label = { Text(label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = CharcoalBackground
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Text Animation Style
          Text("Animation", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            TextAnimation.values().forEach { anim ->
              FilterChip(
                selected = selectedAnimation == anim,
                onClick = { selectedAnimation = anim },
                label = { Text(anim.displayName, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NeonPink,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = TextSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (text.isNotBlank()) {
                onSaveOverlay(
                  text,
                  selectedColorHex,
                  selectedBgHex,
                  fontStyleName,
                  selectedAnimation,
                  fontSizeSp,
                  hasStroke,
                  strokeColorHex,
                  hasShadow,
                  shadowColorHex,
                  alignment,
                  rotationDegrees,
                  scale,
                  durationMs
                )
                onDismiss()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
            modifier = Modifier.testTag("save_text_button")
          ) {
            Text("Apply Text", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
