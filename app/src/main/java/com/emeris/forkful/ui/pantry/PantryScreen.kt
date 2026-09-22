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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.AlertPinkBackground
import com.emeris.forkful.core.designsystem.AlertPinkText
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.MintLight
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.util.PantryUtils
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.ui.components.ErrorState
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.components.LoadingState
import com.emeris.forkful.ui.di.containerViewModel
import com.emeris.forkful.ui.navigation.Screen
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantryScreen(
    onNavigateTo: (String) -> Unit,
    onRecipeSelected: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("PantryScreen", "ON_CREATE")
    }

    val viewModel = containerViewModel { PantryViewModel(it.pantryRepository) }
    val uiState by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingRemoval by remember { mutableStateOf<PantryItem?>(null) }

    LaunchedEffect(uiState.addSuccessMessage, uiState.errorMessage) {
        uiState.addSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessages()
        }
    }

    val visibleItems = uiState.items.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.Pantry.route,
                onNavigateTo = onNavigateTo
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    ForkfulLogger.logAction("PANTRY", "Add item tapped")
                    showAddDialog = true
                },
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
                .verticalScroll(rememberScrollState())
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

                IconButton(onClick = {
                    ForkfulLogger.logAction("SCANNER", "Barcode scanning arrives in the Final POE")
                    showAddDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan",
                        tint = ForestGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(PureWhite)
                    .border(1.dp, BorderLight, RoundedCornerShape(26.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(fontSize = 15.sp, color = TextCharcoal),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(text = "Search your pantry", color = TextMuted, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    }
                )
                if (searchQuery.isNotBlank() && visibleItems.isEmpty()) {
                    Text(
                        text = "Add",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreen,
                        modifier = Modifier.clickable {
                            showAddDialog = true
                        }
                    )
                }
            }

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    LoadingState("Opening the fridge...")
                }
                uiState.errorMessage != null -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    ErrorState(message = uiState.errorMessage.orEmpty(), onRetry = viewModel::load)
                }
                else -> {
                    if (uiState.expiringSoon.isNotEmpty()) {
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

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                uiState.expiringSoon.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AlertPinkBackground)
                                            .clickable { onNavigateTo(Screen.Explore.route) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "${item.name} - ${PantryUtils.expiryLabel(item.daysUntilExpiry)}",
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

                    uiState.bestMatch?.let { match ->
                        Spacer(modifier = Modifier.height(28.dp))

                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = "BEST PANTRY MATCH",
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
                                    .clickable { onRecipeSelected(match.recipe.id) }
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MintLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${match.matchPercentage}%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = match.recipe.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextCharcoal
                                        )
                                        val missing = match.missingIngredients.size
                                        Text(
                                            text = if (missing == 0) "You have everything you need"
                                            else "Missing $missing ingredient${if (missing == 1) "" else "s"} - added to basket",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "PANTRY (${uiState.items.size})",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (visibleItems.isEmpty()) {
                            Text(
                                text = "Your pantry is empty.\nTap + to add your first ingredient.",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            visibleItems.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PureWhite)
                                        .border(1.dp, Color(0xFFECE7DE), RoundedCornerShape(16.dp))
                                        .clickable { pendingRemoval = item }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            fontSize = 13.sp,
                                            color = TextCharcoal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (item.daysUntilExpiry != null) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = PantryUtils.expiryLabel(item.daysUntilExpiry),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (PantryUtils.isExpiringSoon(item.daysUntilExpiry) ||
                                                    PantryUtils.isExpired(item.daysUntilExpiry)
                                                ) AlertPinkText else TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    if (showAddDialog) {
        AddPantryItemDialog(
            isAdding = uiState.isAdding,
            suggestions = viewModel.quickAddSuggestions,
            prefillName = searchQuery.takeIf { it.isNotBlank() },
            onDismiss = {
                showAddDialog = false
                viewModel.consumeMessages()
            },
            onConfirm = { name, quantity, unit, expiryDate ->
                viewModel.addItem(name, quantity, unit, expiryDate)
                showAddDialog = false
            }
        )
    }

    pendingRemoval?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            title = { Text("Remove ${item.name}?") },
            text = { Text("This item will be removed from your Capture Fridge.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeItem(item.id)
                    pendingRemoval = null
                }) {
                    Text("Remove", color = AlertPinkText)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }) {
                    Text("Keep")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddPantryItemDialog(
    isAdding: Boolean,
    suggestions: List<String>,
    prefillName: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: String?, unit: String?, expiryDate: String?) -> Unit
) {
    var name by remember { mutableStateOf(prefillName.orEmpty()) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun submit() {
        when {
            name.isBlank() -> validationError = "Give the ingredient a name."
            expiryDate.isNotBlank() && !dateRegex.matches(expiryDate) ->
                validationError = "Expiry date must look like ${LocalDate.now().plusDays(3)}."
            else -> onConfirm(name.trim(), quantity.trim(), unit.trim(), expiryDate.trim())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Text(
                text = "Add pantry item",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                color = TextCharcoal
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        cursorColor = ForestGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            cursorColor = ForestGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            cursorColor = ForestGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry date (yyyy-mm-dd, optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        cursorColor = ForestGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "QUICK ADD",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.take(10).forEach { suggestion ->
                        if (!suggestion.equals(name, ignoreCase = true)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CreamBackground)
                                    .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                                    .clickable {
                                        name = suggestion
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 12.sp,
                                    color = TextCharcoal
                                )
                            }
                        }
                    }
                }

                validationError?.let { error ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = error, color = AlertPinkText, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                enabled = !isAdding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = PureWhite
                )
            ) {
                Text(if (isAdding) "Adding..." else "Add to pantry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
