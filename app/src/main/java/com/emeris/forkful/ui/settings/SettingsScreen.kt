package com.emeris.forkful.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.navigation.Screen

@Composable
fun SettingsScreen(
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("SettingsScreen", "ON_CREATE")
    }

    var darkModeEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(true) }

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

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Preferences",
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = TextCharcoal
            )

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
                    SettingsNavigationItem(
                        icon = Icons.Default.RestaurantMenu,
                        title = "Dietary filters",
                        onClick = {
                            ForkfulLogger.logAction("SETTINGS", "Dietary filters selected")
                            onNavigateTo(Screen.Onboarding.route)
                        }
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3EFE7)))
                    SettingsNavigationItem(
                        icon = Icons.Default.Refresh,
                        title = "Reset taste profiles",
                        onClick = { ForkfulLogger.logAction("SETTINGS", "Reset taste profiles requested") }
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3EFE7)))
                    SettingsNavigationItem(
                        icon = Icons.Default.Language,
                        title = "Language (English)",
                        onClick = { ForkfulLogger.logAction("SETTINGS", "Language selection tapped") }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Nightlight, contentDescription = null, tint = TextMuted)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Dark mode", fontSize = 15.sp, color = TextCharcoal, modifier = Modifier.weight(1f))
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = {
                                darkModeEnabled = it
                                ForkfulLogger.logAction("SETTINGS_TOGGLE", "Dark mode: $it")
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = ForestGreen)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3EFE7)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = TextMuted)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Biometric lock", fontSize = 15.sp, color = TextCharcoal, modifier = Modifier.weight(1f))
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                ForkfulLogger.logAction("SETTINGS_TOGGLE", "Biometric lock: $it")
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = ForestGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        ForkfulLogger.logAction("AUTH", "User initiated Log Out")
                        onLogout()
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
        }
    }
}

@Composable
fun SettingsNavigationItem(
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
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted
        )
    }
}