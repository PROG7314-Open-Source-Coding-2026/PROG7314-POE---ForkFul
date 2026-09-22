package com.emeris.forkful.ui.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.emeris.forkful.core.designsystem.AccentGreen
import com.emeris.forkful.core.designsystem.AlertRed
import com.emeris.forkful.core.designsystem.ButtonGreen
import com.emeris.forkful.core.designsystem.CardBorder
import com.emeris.forkful.core.designsystem.CardSurface
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.MintLight
import com.emeris.forkful.core.designsystem.SubtleCircleBg
import com.emeris.forkful.core.designsystem.SubtleCircleIcon
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.designsystem.TrueWhite
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch
import com.emeris.forkful.ui.components.ErrorState
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.components.LoadingState
import com.emeris.forkful.ui.di.containerViewModel
import com.emeris.forkful.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    @Suppress("SpellCheckingInspection")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.addSuccessMessage) {
        uiState.addSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessages()
        }
    }

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
                onClick = { showAddSheet = true },
                containerColor = ButtonGreen,
                contentColor = TrueWhite,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add pantry item",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = CreamBackground
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingState("Loading your pantry stock...")
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(
                        message = uiState.errorMessage.orEmpty(),
                        onRetry = viewModel::loadPantry
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(bottom = 80.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "My Pantry",
                                fontFamily = FontFamily.Serif,
                                fontSize = 28.sp,
                                color = TextCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.items.size} ingredients currently tracked",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    }

                    if (uiState.matches.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)) {
                                Text(
                                    text = "Ready to Cook",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    color = TextCharcoal
                                )
                                Text(
                                    text = "Dishes matched with your ingredients",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(uiState.matches) { match ->
                                    PantryMatchCard(
                                        match = match,
                                        onClick = { onRecipeSelected(match.recipe.id) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ingredients in Stock",
                                fontFamily = FontFamily.Serif,
                                fontSize = 20.sp,
                                color = TextCharcoal
                            )
                            Text(
                                text = "Category view",
                                fontSize = 13.sp,
                                color = AccentGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (uiState.items.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Kitchen,
                                        contentDescription = null,
                                        tint = SubtleCircleIcon,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Your pantry is empty",
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 18.sp,
                                        color = TextCharcoal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap the + button below to log your items and discover recipes.",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.items, key = { it.id }) { item ->
                            PantryItemRow(
                                item = item,
                                onDelete = { viewModel.removePantryItem(item.id) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            AddPantryItemBottomSheet(
                sheetState = sheetState,
                onDismiss = { showAddSheet = false },
                onConfirm = { name, quantity, category, daysUntilExpiry ->
                    viewModel.addPantryItem(name, quantity, category, daysUntilExpiry)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showAddSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PantryMatchCard(
    match: PantryMatch,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SubtleCircleBg),
                contentAlignment = Alignment.Center
            ) {
                if (match.recipe.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = match.recipe.imageUrl,
                        contentDescription = match.recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = SubtleCircleIcon,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MintLight)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${match.matchPercentage}% MATCH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ButtonGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = match.recipe.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextCharcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${match.recipe.prepTimeMinutes} min - ${match.missingIngredients.size} missing",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun PantryItemRow(
    item: PantryItem,
    onDelete: () -> Unit
) {
    val days = item.daysUntilExpiry
    val isUrgent = days != null && days in 0..2
    val isExpired = days != null && days < 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SubtleCircleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Kitchen,
                    contentDescription = null,
                    tint = SubtleCircleIcon,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = " - ",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = when {
                            days == null -> "No expiry set"
                            days < 0 -> "Expired"
                            days == 0 -> "Expires today"
                            days == 1 -> "Expires tomorrow"
                            else -> "Expires in $days days"
                        },
                        fontSize = 12.sp,
                        fontWeight = if (isUrgent || isExpired) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUrgent || isExpired) AlertRed else TextMuted
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete item",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}