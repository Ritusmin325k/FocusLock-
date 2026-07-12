package com.example.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import com.example.service.FocusSessionService
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground

class BlockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Auto-dismiss the block screen if the session ends, goes into IDLE, or transitions to BREAK phase
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                FocusSessionService.sessionState.collect { state ->
                    if (state == null ||
                        state.currentPhase == FocusSessionService.FocusPhase.BREAK ||
                        state.currentPhase == FocusSessionService.FocusPhase.COMPLETED ||
                        state.currentState == FocusSessionService.FocusState.IDLE) {
                        finish()
                    }
                }
            }
        }
        
        val blockedPkg = intent.getStringExtra("blocked_package") ?: "Distracting Application"
        // Try to get a user-friendly name from package if possible
        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(blockedPkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            blockedPkg.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: "Distracting App"
        }

        // Intercept back button to return the user to the safe launcher home screen
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                } catch (e: Exception) {}
                finish()
            }
        })

        setContent {
            MyApplicationTheme {
                Scaffold { innerPadding ->
                    BlockScreenContent(
                        appName = appName,
                        onDismiss = {
                            try {
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(homeIntent)
                            } catch (e: Exception) {}
                            finish()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BlockScreenContent(
    appName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = GlassThemeColors(isDark = true) // Always dark/slate focused warning card for impact
    val textColor = themeColors.textColor
    val accentColor = themeColors.accentColor

    LiquidGlassBackground(
        isDark = true,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High quality lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Icon",
                tint = accentColor,
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFF374151).copy(alpha = 0.5f), shape = MaterialTheme.shapes.extraLarge)
                    .padding(24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Focus Session Active",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You'll be able to use $appName during your next break.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = themeColors.secondaryTextColor,
                    lineHeight = 26.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color(0xFF111827)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .widthIn(max = 280.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Back to Studying",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}
