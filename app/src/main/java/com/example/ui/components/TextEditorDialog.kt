package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.TextAnimation
import com.example.ui.theme.*

@Composable
fun TextEditorDialog(
  initialText: String = "",
  onDismiss: () -> Unit,
  onSaveText: (String, String, String?, TextAnimation, Float) -> Unit
) {
  var text by remember { mutableStateOf(initialText) }
  var selectedColorHex by remember { mutableStateOf("#FFFFFF") }
  var selectedBgHex by remember { mutableStateOf<String?>("#99000000") }
  var selectedAnimation by remember { mutableStateOf(TextAnimation.POP) }
  var fontSizeSp by remember { mutableFloatStateOf(26f) }

  val colorPresets = listOf(
    "#FFFFFF" to "White",
    "#FFD54F" to "Gold",
    "#00E676" to "Emerald",
    "#FF2A85" to "Neon Pink",
    "#00E5FF" to "Cyan",
    "#FF6D00" to "Saffron",
    "#FF334B" to "Red"
  )

  val bgPresets = listOf(
    null to "None",
    "#99000000" to "Dark Pill",
    "#9900E676" to "Emerald Pill",
    "#99FFD54F" to "Gold Pill"
  )

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CharcoalSurface,
      border = BorderStroke(1.dp, CharcoalBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text("Text & Typography ✍️", style = MaterialTheme.typography.titleLarge, color = TextPrimary)

        Spacer(modifier = Modifier.height(12.dp))

        // Live Text Preview Card
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color.Black,
          modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
          border = BorderStroke(1.dp, CharcoalBorder)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(android.graphics.Color.parseColor(selectedBgHex ?: "#00000000")),
              modifier = Modifier.padding(8.dp)
            ) {
              Text(
                text = if (text.isNotBlank()) text else "Your Title Here",
                color = Color(android.graphics.Color.parseColor(selectedColorHex)),
                fontSize = (fontSizeSp * 0.7f).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

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

        // Color swatches
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
                .size(32.dp)
                .background(col, CircleShape)
                .border(if (selectedColorHex == hex) 2.dp else 1.dp, if (selectedColorHex == hex) EmeraldPrimary else CharcoalBorder, CircleShape)
                .clickable { selectedColorHex = hex }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Background pill swatches
        Text("Background Style", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          bgPresets.forEach { (hex, label) ->
            FilterChip(
              selected = selectedBgHex == hex,
              onClick = { selectedBgHex = hex },
              label = { Text(label) },
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
              label = { Text(anim.displayName) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonPink,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = TextSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (text.isNotBlank()) {
                onSaveText(text, selectedColorHex, selectedBgHex, selectedAnimation, fontSizeSp)
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
