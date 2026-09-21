package com.emeris.forkful.ui.pantry

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.AlertPinkBackground
import com.emeris.forkful.core.designsystem.AlertPinkText
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.data.repository.MockData
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.navigation.Screen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantryScreen(
    onNavigateTo: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("PantryScreen", "ON_CREATE")
    }

    val pantryItems = MockData.samplePantryItems
    val expiringSoon = pantryItems.filter { item ->
        item.daysUntilExpiry != null && item.daysUntilExpiry <= 2
    }

    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.Pantry.route,
                onNavigateTo = onNavigateTo
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { ForkfulLogger.logAction("PANTRY", "Add item tapped") },
                containerColor = ForestGreen,
                contentColor = PureWhite,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ingredient")
            }
        },
        containerColor = CreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    text = "Pantry",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    color = ForestGreen
                )

                IconButton(onClick = { ForkfulLogger.logAction("SCANNER", "Camera fridge barcode triggered") }) {
                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = "Scan",
                        tint = ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(PureWhite)
                    .border(1.dp, BorderLight, RoundedCornerShape(26.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search or add an item",
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "EXPIRING SOON",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    expiringSoon.forEach { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AlertPinkBackground)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${item.name} - ${item.daysUntilExpiry}d",
                                color = AlertPinkText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "PANTRY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pantryItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(PureWhite)
                                .border(1.dp, Color(0xFFECE7DE), RoundedCornerShape(16.dp))
                                .clickable {
                                    ForkfulLogger.logAction("PANTRY_ITEM", "Clicked ${item.name}")
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 13.sp,
                                color = TextCharcoal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}