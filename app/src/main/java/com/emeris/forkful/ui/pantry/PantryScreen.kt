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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
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
import com.emeris.forkful.domain.model.PantryItem
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

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var itemToRemove by remember { mutableStateOf<PantryItem?>(null) }

    val pantryItems = remember {
        mutableStateListOf<PantryItem>().apply { addAll(MockData.samplePantryItems) }
    }

    val expiringSoon = pantryItems.filter { item ->
        item.daysUntilExpiry != null && item.daysUntilExpiry <= 2
    }
    val visibleItems = pantryItems.filter { item ->
        item.name.contains(searchQuery, ignoreCase = true)
    }

    val scrollState = rememberScrollState()
    val chipShape = RoundedCornerShape(16.dp)
    val searchShape = RoundedCornerShape(26.dp)

    Scaffold(
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.Pantry.route,
                onNavigateTo = onNavigateTo
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ForestGreen,
                contentColor = PureWhite,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
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
                    .shadow(6.dp, searchShape, spotColor = Color(0x33000000))
                    .clip(searchShape)
                    .background(PureWhite)
                    .border(1.dp, BorderLight, searchShape)
                    .height(52.dp)
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
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search or add an item",
                                color = TextMuted,
                                fontSize = 15.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextCharcoal,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(ForestGreen),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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

                if (expiringSoon.isEmpty()) {
                    Text(
                        text = "Nothing is expiring soon",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        expiringSoon.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .shadow(4.dp, chipShape, spotColor = Color(0x33000000))
                                    .clip(chipShape)
                                    .background(AlertPinkBackground)
                                    .clickable { itemToRemove = item }
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

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap an item to remove it",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (visibleItems.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) {
                            "Your pantry is empty"
                        } else {
                            "No pantry items match \"$searchQuery\""
                        },
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        visibleItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .shadow(3.dp, chipShape, spotColor = Color(0x26000000))
                                    .clip(chipShape)
                                    .background(PureWhite)
                                    .border(1.dp, Color(0xFFECE7DE), chipShape)
                                    .clickable { itemToRemove = item }
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
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add pantry item") },
            text = {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    singleLine = true,
                    label = { Text("Ingredient name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedName = newItemName.trim()
                        if (trimmedName.isNotEmpty()) {
                            pantryItems.add(
                                0,
                                PantryItem(
                                    id = "p${System.currentTimeMillis()}",
                                    name = trimmedName,
                                    category = "Custom"
                                )
                            )
                            ForkfulLogger.logAction("PANTRY", "Added $trimmedName")
                            newItemName = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    itemToRemove?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            title = { Text("Remove item") },
            text = { Text("Remove ${item.name} from your pantry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pantryItems.removeAll { it.id == item.id }
                        ForkfulLogger.logAction("PANTRY", "Removed ${item.name}")
                        itemToRemove = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}