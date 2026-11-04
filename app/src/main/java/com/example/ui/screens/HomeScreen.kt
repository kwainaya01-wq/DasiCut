package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.AspectRatio
import com.example.model.Project
import com.example.ui.components.formatTimecode
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.EditorViewModel

@Composable
fun HomeScreen(
  viewModel: EditorViewModel,
  onNavigateToEditor: (Project) -> Unit,
  modifier: Modifier = Modifier
) {
  val projects by viewModel.projects.collectAsState()
  var showNewProjectDialog by remember { mutableStateOf(false) }
  var projectToRename by remember { mutableStateOf<Project?>(null) }
  var projectToDelete by remember { mutableStateOf<Project?>(null) }

  // Photo / Video picker using modern zero-permission Android Photo Picker
  val mediaPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.createProjectWithVideo(uri) { newProj ->
        onNavigateToEditor(newProj)
      }
    }
  }

  Scaffold(
    containerColor = CharcoalBackground,
    topBar = {
      HomeTopBar(
        onNewClick = { showNewProjectDialog = true }
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. Hero Action Cards
      item {
        HeroActionsSection(
          onNewVideoClick = { showNewProjectDialog = true },
          onPickMediaClick = {
            mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
          },
          onEditPhotoClick = {
            mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          }
        )
      }

      // 2. Quick Editing Tools Grid
      item {
        QuickToolsSection(
          onToolClick = { toolName ->
            when (toolName) {
              "Auto Captions" -> {
                val proj = projects.firstOrNull() ?: viewModel.repository.createNewProject()
                onNavigateToEditor(proj)
                viewModel.generateAutoCaptions()
              }
              "Adjust Speed" -> {
                val proj = projects.firstOrNull() ?: viewModel.repository.createNewProject()
                onNavigateToEditor(proj)
              }
              "AI Voiceover" -> {
                viewModel.navigateTo(AppScreen.AI_LAB)
              }
              "Professional Editor" -> {
                showNewProjectDialog = true
              }
              "Owner Studio" -> {
                viewModel.navigateTo(AppScreen.OWNER_STUDIO)
              }
              else -> {
                viewModel.navigateTo(AppScreen.AI_LAB)
              }
            }
          }
        )
      }

      // 3. Recent Projects Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Projects (${projects.size})",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          if (projects.isNotEmpty()) {
            Text(
              text = "+ New Project",
              style = MaterialTheme.typography.labelMedium,
              color = EmeraldPrimary,
              modifier = Modifier.clickable { showNewProjectDialog = true }
            )
          }
        }
      }

      if (projects.isEmpty()) {
        item {
          EmptyProjectsCard(onCreateNew = { showNewProjectDialog = true })
        }
      } else {
        items(projects) { project ->
          ProjectItemCard(
            project = project,
            onClick = { onNavigateToEditor(project) },
            onRename = { projectToRename = project },
            onDuplicate = { viewModel.duplicateProject(project.id) },
            onDelete = { projectToDelete = project }
          )
        }
      }

      // 4. Creator Inspiration & Tips Card
      item {
        InspirationBanner(onExploreTemplates = { viewModel.navigateTo(AppScreen.TEMPLATES) })
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // New Project Dialog with Name & Aspect Ratio selector
  if (showNewProjectDialog) {
    NewProjectModal(
      onDismiss = { showNewProjectDialog = false },
      onCreate = { name, ratio ->
        viewModel.createNewProject(name, ratio)
        showNewProjectDialog = false
      }
    )
  }

  // Rename Dialog
  projectToRename?.let { proj ->
    RenameProjectModal(
      initialName = proj.name,
      onDismiss = { projectToRename = null },
      onRename = { newName ->
        viewModel.renameProject(proj.id, newName)
        projectToRename = null
      }
    )
  }

  // Delete Confirmation Dialog
  projectToDelete?.let { proj ->
    AlertDialog(
      onDismissRequest = { projectToDelete = null },
      title = { Text("Delete Project?", color = TextPrimary) },
      text = { Text("Are you sure you want to delete '${proj.name}'? This action cannot be undone.", color = TextSecondary) },
      containerColor = CharcoalSurface,
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteProject(proj.id)
            projectToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { projectToDelete = null }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun HomeTopBar(onNewClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "DesiCut",
          style = MaterialTheme.typography.displayMedium,
          color = EmeraldPrimary,
          fontSize = 24.sp
        )
        Text(
          text = " STUDIO",
          style = MaterialTheme.typography.displayMedium,
          color = TextPrimary,
          fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "🇮🇳", fontSize = 18.sp)
      }
      Text(
        text = "Create. Edit. Inspire.",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        letterSpacing = 0.5.sp
      )
    }

    // Pro Badge & New button
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = CharcoalCard,
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
          Text("PRO", style = MaterialTheme.typography.labelSmall, color = GoldAccent, fontWeight = FontWeight.Bold)
        }
      }

      FilledIconButton(
        onClick = onNewClick,
        modifier = Modifier.size(36.dp).testTag("home_add_project_button"),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground)
      ) {
        Icon(Icons.Default.Add, contentDescription = "New Project", modifier = Modifier.size(20.dp))
      }
    }
  }
}

@Composable
private fun HeroActionsSection(
  onNewVideoClick: () -> Unit,
  onPickMediaClick: () -> Unit,
  onEditPhotoClick: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Large Primary Hero Card: "New Video"
    Surface(
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .weight(1.3f)
        .height(130.dp)
        .clickable { onNewVideoClick() }
        .testTag("hero_new_video_card"),
      color = CharcoalCard
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFF00B09B), Color(0xFF004D40))
            )
          )
          .padding(14.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0x33FFFFFF),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
          }

          Column {
            Text("New Video", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Multi-track timeline", style = MaterialTheme.typography.labelSmall, color = Color(0xCCFFFFFF))
          }
        }
      }
    }

    // Secondary Cards Column: "Pick Media" & "Edit Photo"
    Column(
      modifier = Modifier
        .weight(1f)
        .height(130.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Pick Media
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = CharcoalCard,
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clickable { onPickMediaClick() }
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
          Text("Import Video", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        }
      }

      // Edit Photo
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = CharcoalCard,
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clickable { onEditPhotoClick() }
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
          Text("Edit Photo", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        }
      }
    }
  }
}

@Composable
private fun QuickToolsSection(onToolClick: (String) -> Unit) {
  val tools = listOf(
    Pair("Remove BG", Icons.Default.Face),
    Pair("Auto Captions", Icons.Default.Subtitles),
    Pair("Adjust Speed", Icons.Default.Speed),
    Pair("Owner Studio", Icons.Default.AdminPanelSettings),
    Pair("AI Voiceover", Icons.Default.RecordVoiceOver),
    Pair("Professional Editor", Icons.Default.MovieFilter),
    Pair("All Tools", Icons.Default.DashboardCustomize)
  )

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Quick Creation Tools",
      style = MaterialTheme.typography.titleMedium,
      color = TextSecondary,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(tools) { (name, icon) ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier
            .clickable { onToolClick(name) }
            .testTag("quick_tool_$name")
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = CharcoalSurfaceVariant,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = name, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(name, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun ProjectItemCard(
  project: Project,
  onClick: () -> Unit,
  onRename: () -> Unit,
  onDuplicate: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = CharcoalCard,
    border = BorderStroke(1.dp, CharcoalBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("project_card_${project.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Thumbnail gradient frame preview
        val firstClip = project.clips.firstOrNull()
        val startCol = try { Color(android.graphics.Color.parseColor(firstClip?.colorStartHex ?: "#00E676")) } catch (_: Exception) { EmeraldDark }
        val endCol = try { Color(android.graphics.Color.parseColor(firstClip?.colorEndHex ?: "#00B09B")) } catch (_: Exception) { EmeraldPrimary }

        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(startCol, endCol)))
            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))

          // Duration badge in corner
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xAA000000),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(3.dp)
          ) {
            Text(
              text = formatTimecode(project.totalDurationMs).take(5),
              color = Color.White,
              fontSize = 8.sp,
              modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
            )
          }
        }

        // Details Column
        Column {
          Text(
            text = project.name,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = CharcoalSurfaceVariant
            ) {
              Text(
                text = project.aspectRatio.label,
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldPrimary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
            Text(
              text = "• ${project.clips.size} clips",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted
            )
            Text(
              text = "• ${project.quality}",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted
            )
          }
        }
      }

      // Three dots overflow menu
      Box {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = "Project Options", tint = TextMuted)
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false },
          modifier = Modifier.background(CharcoalSurface)
        ) {
          DropdownMenuItem(
            text = { Text("Open Project", color = TextPrimary) },
            onClick = {
              showMenu = false
              onClick()
            },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = EmeraldPrimary) }
          )
          DropdownMenuItem(
            text = { Text("Rename", color = TextPrimary) },
            onClick = {
              showMenu = false
              onRename()
            },
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = TextSecondary) }
          )
          DropdownMenuItem(
            text = { Text("Duplicate", color = TextPrimary) },
            onClick = {
              showMenu = false
              onDuplicate()
            },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSecondary) }
          )
          HorizontalDivider(color = CharcoalBorder)
          DropdownMenuItem(
            text = { Text("Delete", color = ErrorRed) },
            onClick = {
              showMenu = false
              onDelete()
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) }
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyProjectsCard(onCreateNew: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = CharcoalCard,
    border = BorderStroke(1.dp, CharcoalBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(Icons.Default.MovieCreation, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(48.dp))
      Spacer(modifier = Modifier.height(12.dp))
      Text("No Projects Yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
      Spacer(modifier = Modifier.height(4.dp))
      Text("Start creating viral Reels and Shorts with multi-track timeline editing.", style = MaterialTheme.typography.bodyMedium, color = TextMuted, fontSize = 12.sp)
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = onCreateNew,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground)
      ) {
        Text("Create First Video", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun InspirationBanner(onExploreTemplates: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = CharcoalCard,
    border = BorderStroke(1.dp, CharcoalBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onExploreTemplates() }
  ) {
    Row(
      modifier = Modifier
        .background(
          Brush.horizontalGradient(
            listOf(Color(0xFF1F2937), Color(0xFF111827))
          )
        )
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = GoldAccent.copy(alpha = 0.2f),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
          }
        }
        Column {
          Text("Trending Video Templates 🔥", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
          Text("1-Tap beat synced Reels, Bollywood slow-mo & Shorts", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
      }

      Icon(Icons.Default.ArrowForward, contentDescription = null, tint = EmeraldPrimary)
    }
  }
}

@Composable
private fun NewProjectModal(
  onDismiss: () -> Unit,
  onCreate: (String, AspectRatio) -> Unit
) {
  var name by remember { mutableStateOf("Untitled Video") }
  var selectedRatio by remember { mutableStateOf(AspectRatio.RATIO_9_16) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create New Project", color = TextPrimary) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Project Title") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
        )

        Text("Select Canvas Ratio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AspectRatio.values().take(3).forEach { ratio ->
            val isSelected = selectedRatio == ratio
            FilterChip(
              selected = isSelected,
              onClick = { selectedRatio = ratio },
              label = { Text(ratio.label) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = CharcoalBackground
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    },
    containerColor = CharcoalSurface,
    confirmButton = {
      Button(
        onClick = { onCreate(name.ifBlank { "Untitled Video" }, selectedRatio) },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
        modifier = Modifier.testTag("create_project_confirm_button")
      ) {
        Text("Start Editing", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}

@Composable
private fun RenameProjectModal(
  initialName: String,
  onDismiss: () -> Unit,
  onRename: (String) -> Unit
) {
  var name by remember { mutableStateOf(initialName) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Rename Project", color = TextPrimary) },
    text = {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Project Name") },
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldPrimary,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        modifier = Modifier.fillMaxWidth()
      )
    },
    containerColor = CharcoalSurface,
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) onRename(name)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground)
      ) {
        Text("Rename", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
