package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPickerSheet(
  onDismiss: () -> Unit,
  onStickerSelected: (String, String) -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = CharcoalSurface,
    dragHandle = { BottomSheetDefaults.DragHandle(color = CharcoalBorder) }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Desi Stickers & Emojis ✨", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.heightIn(max = 280.dp)
      ) {
        items(SampleData.stickersList) { sticker ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = CharcoalCard,
            modifier = Modifier
              .clickable {
                onStickerSelected(sticker.emojiOrSymbol, sticker.label)
                onDismiss()
              }
              .height(72.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(4.dp)
            ) {
              Text(sticker.emojiOrSymbol, fontSize = 28.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Text(sticker.label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
            }
          }
        }
      }
    }
  }
}
