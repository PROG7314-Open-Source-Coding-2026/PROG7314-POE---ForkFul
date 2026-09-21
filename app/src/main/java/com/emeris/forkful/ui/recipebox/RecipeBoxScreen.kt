package com.emeris.forkful.ui.recipebox

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.MintLight
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.data.repository.MockData
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.navigation.Screen

@Composable
fun RecipeBoxScreen(
    onNavigateTo: (String) -> Unit,
    onRecipeSelected: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("RecipeBoxScreen", "ON_CREATE")
    }

    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val tabs = listOf("All", "Saved", "Cooked")

    val displayedRecipes = remember(selectedTab, searchQuery) {
        val tabRecipes = when (selectedTab) {
            "Saved" -> MockData.sampleRecipes.filter { it.isSaved }
            "Cooked" -> MockData.sampleRecipes.filter { it.isCooked }
            else -> MockData.sampleRecipes
        }
        tabRecipes.filter { recipe ->
            recipe.title.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.RecipeBox.route,
                onNavigateTo = onNavigateTo
            )
        },
        containerColor = CreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { ForkfulLogger.logAction("DRAWER", "Menu tapped") }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = ForestGreen
                    )
                }

                Text(
                    text = "Recipe Box",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    color = ForestGreen,
                    fontWeight = FontWeight.Normal
                )

                IconButton(
                    onClick = {
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    }
                ) {
                    Icon(
                        imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (showSearch) "Close search" else "Search",
                        tint = ForestGreen
                    )
                }
            }

            if (showSearch) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PureWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search saved recipes",
                            color = TextMuted,
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = TextCharcoal, fontSize = 15.sp),
                        cursorBrush = SolidColor(ForestGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tabs.forEach { tab ->
                    val isTabSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isTabSelected) ForestGreen else PureWhite)
                            .border(
                                width = 1.dp,
                                color = if (isTabSelected) ForestGreen else BorderLight,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isTabSelected) PureWhite else TextCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displayedRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "No recipes match \"$searchQuery\""
                        } else {
                            "No recipes in $selectedTab yet"
                        },
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedRecipes) { recipe ->
                        RecipeGridCard(
                            recipe = recipe,
                            onClick = { onRecipeSelected(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeGridCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                val badgeText = when {
                    recipe.isCooked -> "Cooked"
                    recipe.isSaved -> "Saved"
                    recipe.matchPercentage > 90 -> "${recipe.matchPercentage}% match"
                    else -> "Recipe"
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MintLight.copy(alpha = 0.95f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreen
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.title,
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    color = TextCharcoal
                )
            }
        }
    }
}