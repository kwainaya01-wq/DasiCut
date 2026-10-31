package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SampleData
import com.example.model.Template
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorViewModel

@Composable
fun TemplatesScreen(
  viewModel: EditorViewModel,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("All") }
  var previewTemplate by remember { mutableStateOf<Template?>(null) }

  val categories = listOf(
    "All", "Trending", "Reels", "Shorts", "Cinematic",
    "Birthday", "Love", "Travel", "Attitude", "Status", "Beat Sync"
  )

  val filteredTemplates = remember(selectedCategory) {
    if (selectedCategory == "All") {
      SampleData.templates
    } else {
      SampleData.templates.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        .ifEmpty { SampleData.templates }
    }
  }

  Scaffold(
    containerColor = CharcoalBackground,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Template Studio ✨", style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontSize = 24.sp)
          Text("Instant viral Reels & Shorts templates", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Top Indian Hits", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
          }
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Categories horizontal chip row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          val isSelected = selectedCategory == cat
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = cat },
            label = { Text(cat) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = CharcoalBackground,
              containerColor = CharcoalCard,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = if (isSelected) EmeraldPrimary else CharcoalBorder
            )
          )
        }
      }

      // Templates 2-column grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredTemplates) { template ->
          TemplateCard(
            template = template,
            onClick = { previewTemplate = template }
          )
        }
      }
    }
  }

  // Interactive Template Preview Modal
  previewTemplate?.let { tpl ->
    TemplatePreviewModal(
      template = tpl,
      onDismiss = { previewTemplate = null },
      onUseTemplate = {
        viewModel.useTemplate(tpl)
        previewTemplate = null
      }
    )
  }
}

@Composable
private fun TemplateCard(
  template: Template,
  onClick: () -> Unit
) {
  val startCol = try { Color(android.graphics.Color.parseColor(template.gradientColors.first)) } catch (_: Exception) { EmeraldDark }
  val endCol = try { Color(android.graphics.Color.parseColor(template.gradientColors.second)) } catch (_: Exception) { EmeraldPrimary }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = CharcoalCard,
    border = BorderStroke(1.dp, CharcoalBorder),
    modifier = Modifier
      .fillMaxWidth()
      .height(230.dp)
      .clickable { onClick() }
      .testTag("template_card_${template.id}")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Thumbnail area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(Brush.verticalGradient(listOf(startCol, endCol)))
          .padding(8.dp)
      ) {
        // Aspect ratio tag
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color(0xAA000000),
          modifier = Modifier.align(Alignment.TopStart)
        ) {
          Text(
            text = template.aspectRatio.label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }

        // Use count badge
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color(0xAA000000),
          modifier = Modifier.align(Alignment.TopEnd)
        ) {
          Text(
            text = "${template.usesCount} uses",
            style = MaterialTheme.typography.labelSmall,
            color = GoldAccent,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }

        // Play icon in center
        Surface(
          shape = CircleShape,
          color = Color(0x55000000),
          modifier = Modifier
            .size(36.dp)
            .align(Alignment.Center)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          }
        }

        // BGM label at bottom of preview
        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = template.bgmName,
            color = Color.White,
            fontSize = 9.sp,
            maxLines = 1
          )
        }
      }

      // Title & Slots info
      Column(modifier = Modifier.padding(10.dp)) {
        Text(
          text = template.title,
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${template.requiredClipsCount} clips • ${(template.durationMs / 1000f).toInt()}s",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 11.sp
          )
          Text(
            text = "${template.bpm} BPM",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldPrimary,
            fontSize = 10.sp
          )
        }
      }
    }
  }
}

@Composable
private fun TemplatePreviewModal(
  template: Template,
  onDismiss: () -> Unit,
  onUseTemplate: () -> Unit
) {
  val startCol = try { Color(android.graphics.Color.parseColor(template.gradientColors.first)) } catch (_: Exception) { EmeraldDark }
  val endCol = try { Color(android.graphics.Color.parseColor(template.gradientColors.second)) } catch (_: Exception) { EmeraldPrimary }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CharcoalSurface,
      border = BorderStroke(1.dp, CharcoalBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(template.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Animated video frame container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(startCol, endCol))),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
              shape = CircleShape,
              color = Color(0x66000000),
              modifier = Modifier.size(48.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(28.dp))
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(template.tagline, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Medium)
          }

          // Bottom music pill
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xCC000000),
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(8.dp)
          ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MusicNote, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(template.bgmName, color = Color.White, fontSize = 10.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Template details breakdown
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CharcoalCard,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Clips Needed", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text("${template.requiredClipsCount} videos", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Duration", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text("${(template.durationMs / 1000f).toInt()} sec", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Aspect", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(template.aspectRatio.label, style = MaterialTheme.typography.titleMedium, color = EmeraldPrimary)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Use Template" button
        Button(
          onClick = onUseTemplate,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("use_template_button")
        ) {
          Icon(Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Use This Template", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }
    }
  }
}
