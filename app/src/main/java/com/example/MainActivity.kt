package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      DesiCutTheme {
        DesiCutApp()
      }
    }
  }
}

@Composable
fun DesiCutApp(
  viewModel: EditorViewModel = viewModel()
) {
  val currentScreen by viewModel.currentScreen.collectAsState()

  Scaffold(
    containerColor = CharcoalBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    bottomBar = {
      // Show bottom navigation bar only on root screens (Home, Templates, AI Lab, Profile)
      if (currentScreen != AppScreen.EDITOR && currentScreen != AppScreen.OWNER_STUDIO) {
        NavigationBar(
          containerColor = CharcoalSurface,
          contentColor = TextPrimary,
          tonalElevation = 8.dp,
          modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("main_navigation_bar")
        ) {
          NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { viewModel.navigateTo(AppScreen.HOME) },
            icon = {
              Icon(
                imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.MovieCreation else Icons.Outlined.MovieCreation,
                contentDescription = "Edit"
              )
            },
            label = { Text("Edit") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = CharcoalBackground,
              selectedTextColor = EmeraldPrimary,
              indicatorColor = EmeraldPrimary,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_home")
          )

          NavigationBarItem(
            selected = currentScreen == AppScreen.TEMPLATES,
            onClick = { viewModel.navigateTo(AppScreen.TEMPLATES) },
            icon = {
              Icon(
                imageVector = if (currentScreen == AppScreen.TEMPLATES) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                contentDescription = "Templates"
              )
            },
            label = { Text("Templates") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = CharcoalBackground,
              selectedTextColor = EmeraldPrimary,
              indicatorColor = EmeraldPrimary,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_templates")
          )

          NavigationBarItem(
            selected = currentScreen == AppScreen.AI_LAB,
            onClick = { viewModel.navigateTo(AppScreen.AI_LAB) },
            icon = {
              Icon(
                imageVector = if (currentScreen == AppScreen.AI_LAB) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                contentDescription = "AI Lab"
              )
            },
            label = { Text("AI Lab") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = CharcoalBackground,
              selectedTextColor = EmeraldPrimary,
              indicatorColor = EmeraldPrimary,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_ai_lab")
          )

          NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
            icon = {
              Icon(
                imageVector = if (currentScreen == AppScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                contentDescription = "Profile"
              )
            },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = CharcoalBackground,
              selectedTextColor = EmeraldPrimary,
              indicatorColor = EmeraldPrimary,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_tab_profile")
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentScreen) {
        AppScreen.HOME -> {
          HomeScreen(
            viewModel = viewModel,
            onNavigateToEditor = { project ->
              viewModel.openProject(project)
            }
          )
        }
        AppScreen.EDITOR -> {
          EditorScreen(
            viewModel = viewModel,
            onNavigateBack = {
              viewModel.navigateTo(AppScreen.HOME)
            }
          )
        }
        AppScreen.TEMPLATES -> {
          TemplatesScreen(viewModel = viewModel)
        }
        AppScreen.AI_LAB -> {
          AiLabScreen(viewModel = viewModel)
        }
        AppScreen.PROFILE -> {
          ProfileScreen(viewModel = viewModel)
        }
        AppScreen.OWNER_STUDIO -> {
          OwnerStudioScreen(
            viewModel = viewModel,
            onNavigateBack = {
              viewModel.navigateTo(AppScreen.PROFILE)
            }
          )
        }
      }
    }
  }
}
