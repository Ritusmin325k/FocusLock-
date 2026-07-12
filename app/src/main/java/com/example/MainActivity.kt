package com.example

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.FocusSessionService
import com.example.ui.BreakScreen
import com.example.ui.CreateSessionScreen
import com.example.ui.DashboardScreen
import com.example.ui.FocusScreen
import com.example.ui.SettingsScreen
import com.example.ui.StatisticsScreen
import com.example.ui.SummaryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FocusViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handle result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val viewModel: FocusViewModel = viewModel()
            val isDarkTheme = viewModel.isDarkTheme()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val sessionState by viewModel.serviceState.collectAsState()
                val statsSummary by viewModel.statsSummary.collectAsState()

                // Check for active session state to enforce focus state overriding
                LaunchedEffect(sessionState) {
                    val state = sessionState
                    if (state != null && state.currentState != FocusSessionService.FocusState.IDLE) {
                        if (state.currentPhase == FocusSessionService.FocusPhase.STUDY) {
                            if (currentScreen != FocusViewModel.AppScreen.FOCUS) {
                                viewModel.navigateTo(FocusViewModel.AppScreen.FOCUS)
                            }
                        } else if (state.currentPhase == FocusSessionService.FocusPhase.BREAK) {
                            if (currentScreen != FocusViewModel.AppScreen.BREAK) {
                                viewModel.navigateTo(FocusViewModel.AppScreen.BREAK)
                            }
                        }
                    }
                }

                // Check if user has granted usage permission, provide helpful check method
                val context = LocalContext.current
                var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }

                // Check again whenever the activity resumes
                LaunchedEffect(Unit) {
                    hasUsagePermission = hasUsageStatsPermission(context)
                }

                val showNavigationBar = sessionState == null || sessionState?.currentState == FocusSessionService.FocusState.IDLE

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showNavigationBar) {
                            FocusBottomBar(
                                currentScreen = currentScreen,
                                isDark = isDarkTheme,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                FocusViewModel.AppScreen.DASHBOARD -> {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        stats = statsSummary,
                                        onStartSessionClick = {
                                            // Check usage stats permission if apps are to be blocked
                                            if (!hasUsagePermission) {
                                                requestUsageAccess(context)
                                            }
                                            viewModel.navigateTo(FocusViewModel.AppScreen.CREATE_SESSION)
                                        }
                                    )
                                }
                                FocusViewModel.AppScreen.CREATE_SESSION -> {
                                    CreateSessionScreen(viewModel = viewModel)
                                }
                                FocusViewModel.AppScreen.FOCUS -> {
                                    sessionState?.let {
                                        FocusScreen(viewModel = viewModel, sessionState = it)
                                    } ?: run {
                                        viewModel.navigateTo(FocusViewModel.AppScreen.DASHBOARD)
                                    }
                                }
                                FocusViewModel.AppScreen.BREAK -> {
                                    sessionState?.let {
                                        BreakScreen(viewModel = viewModel, sessionState = it)
                                    } ?: run {
                                        viewModel.navigateTo(FocusViewModel.AppScreen.DASHBOARD)
                                    }
                                }
                                FocusViewModel.AppScreen.SUMMARY -> {
                                    SummaryScreen(
                                        viewModel = viewModel,
                                        sessionState = sessionState,
                                        onBackToDashboard = {
                                            viewModel.navigateTo(FocusViewModel.AppScreen.DASHBOARD)
                                        }
                                    )
                                }
                                FocusViewModel.AppScreen.STATISTICS -> {
                                    StatisticsScreen(viewModel = viewModel, stats = statsSummary)
                                }
                                FocusViewModel.AppScreen.SETTINGS -> {
                                    SettingsScreen(viewModel = viewModel, stats = statsSummary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageAccess(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback if settings page is not reachable on certain devices
        }
    }
}

@Composable
fun FocusBottomBar(
    currentScreen: FocusViewModel.AppScreen,
    isDark: Boolean,
    onNavigate: (FocusViewModel.AppScreen) -> Unit
) {
    val barColor = if (isDark) Color(0xFF111827).copy(alpha = 0.85f) else Color(0xFFF9FAFB).copy(alpha = 0.85f)
    val indicatorColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    val selectedItemColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    val unselectedItemColor = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)

    NavigationBar(
        containerColor = barColor,
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("app_navigation_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen == FocusViewModel.AppScreen.DASHBOARD || currentScreen == FocusViewModel.AppScreen.CREATE_SESSION,
            onClick = { onNavigate(FocusViewModel.AppScreen.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedItemColor,
                selectedTextColor = selectedItemColor,
                indicatorColor = indicatorColor,
                unselectedIconColor = unselectedItemColor,
                unselectedTextColor = unselectedItemColor
            ),
            modifier = Modifier.testTag("nav_item_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == FocusViewModel.AppScreen.STATISTICS,
            onClick = { onNavigate(FocusViewModel.AppScreen.STATISTICS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.InsertChart,
                    contentDescription = "Statistics"
                )
            },
            label = { Text("Statistics", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedItemColor,
                selectedTextColor = selectedItemColor,
                indicatorColor = indicatorColor,
                unselectedIconColor = unselectedItemColor,
                unselectedTextColor = unselectedItemColor
            ),
            modifier = Modifier.testTag("nav_item_statistics")
        )

        NavigationBarItem(
            selected = currentScreen == FocusViewModel.AppScreen.SETTINGS,
            onClick = { onNavigate(FocusViewModel.AppScreen.SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedItemColor,
                selectedTextColor = selectedItemColor,
                indicatorColor = indicatorColor,
                unselectedIconColor = unselectedItemColor,
                unselectedTextColor = unselectedItemColor
            ),
            modifier = Modifier.testTag("nav_item_settings")
        )
    }
}
