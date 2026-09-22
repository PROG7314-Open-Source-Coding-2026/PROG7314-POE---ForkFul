package com.emeris.forkful.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.di.containerViewModel
import com.emeris.forkful.ui.navigation.Screen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("SettingsScreen", "ON_CREATE")
    }

    val viewModel = containerViewModel {
        SettingsViewModel(
            it.preferencesRepository,
            it.authRepository,
            it.sessionManager
        )
    }
    val uiState by viewModel.state.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var dietaryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLogout()
    }

    Scaffold(
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.Settings.route,
                onNavigateTo = onNavigateTo
            )
        },
        containerColor = CreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = ForestGreen
                    )
                }

                Text(
                    text = "Forkful",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    color = ForestGreen
                )

                IconButton(onClick = { ForkfulLogger.logAction("SETTINGS", "Search tapped") }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.preferences.displayName?.let { name ->
                Text(
                    text = "Signed in as $name",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = "Preferences",
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = TextCharcoal
            )

            uiState.infoMessage?.let { message ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = message, fontSize = 13.sp, color = ForestGreen)
            }
            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = message, fontSize = 13.sp, color = com.emeris.forkful.core.designsystem.AlertPinkText)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "ACCOUNT",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PureWhite)
                    .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            ) {
                Column {
                    SettingsExpansionItem(
                        icon = Icons.Default.RestaurantMenu,
                        title = "Dietary filters",
                        subtitle = uiState.preferences.dietaryTags.joinToString(", ").ifBlank { "None set" },
                        expanded = dietaryExpanded
                    ) {
                        dietaryExpanded = !dietaryExpanded
                    }

                    if (dietaryExpanded) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            viewModel.dietaryOptions.forEach { option ->
                                val selected = option in uiState.preferences.dietaryTags
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) ForestGreen else CreamBackground)
                                        .border(
                                            1.dp,
                                            if (selected) ForestGreen else BorderLight,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.toggleDietary(option) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 12.sp,
                                        color = if (selected) PureWhite else TextCharcoal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    SettingsDivider()

                    SettingsActionItem(
                        icon = Icons.Default.Refresh,
                        title = "Reset taste profiles",
                        onClick = { showResetConfirm = true }
                    )

                    SettingsDivider()

                    SettingsActionItem(
                        icon = Icons.Default.Language,
                        title = "Language (${
                            viewModel.languageOptions
                                .firstOrNull { it.key == uiState.preferences.language }?.label
                                ?: "English"
                        })",
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "APP",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PureWhite)
                    .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            ) {
                Column {
                    SettingsToggleItem(
                        icon = Icons.Default.Nightlight,
                        title = "Dark mode",
                        checked = uiState.preferences.theme == "dark",
                        onCheckedChange = viewModel::setDarkMode
                    )

                    SettingsDivider()

                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        checked = uiState.preferences.notificationsEnabled,
                        onCheckedChange = viewModel::setNotifications
                    )

                    SettingsDivider()

                    SettingsToggleItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric lock (Final POE)",
                        checked = uiState.preferences.biometricLockEnabled,
                        onCheckedChange = viewModel::setBiometricLock
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        ForkfulLogger.logAction("AUTH", "User initiated Log Out")
                        viewModel.logout()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log Out",
                    color = Color(0xFFA33B39),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("App language") },
            text = {
                Column {
                    Text(
                        text = "Full in-app translation ships in the Final POE; your preference is stored now.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    viewModel.languageOptions.forEach { option ->
                        val selected = option.key == uiState.preferences.language
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(option.key)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${option.emoji}  ${option.label}", fontSize = 15.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            if (selected) {
                                Text(
                                    text = "SELECTED",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset taste profiles?") },
            text = { Text("All tag points across every mood profile will be zeroed. Your swipe deck will start learning from scratch.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetTasteProfiles()
                    showResetConfirm = false
                }) {
                    Text("Reset", color = ForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF3EFE7))
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 15.sp, color = TextCharcoal, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = {
                ForkfulLogger.logAction("SETTINGS_TOGGLE", "$title: $it")
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = ForestGreen
            )
        )
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 15.sp, color = TextCharcoal, modifier = Modifier.weight(1f))
        Text(
            text = ">",
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )
    }
}

@Composable
private fun SettingsExpansionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = TextCharcoal)
            Text(text = subtitle, fontSize = 12.sp, color = TextMuted)
        }
        Text(
            text = if (expanded) "v" else ">",
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )
    }
}
