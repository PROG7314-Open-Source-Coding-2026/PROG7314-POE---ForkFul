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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.RecipeBoxFilter
import com.emeris.forkful.ui.components.ErrorState
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.components.LoadingState
import com.emeris.forkful.ui.di.containerViewModel
import com.emeris.forkful.ui.navigation.Screen

@Composable
fun RecipeBoxScreen(
    onNavigateTo: (String) -> Unit,
    onRecipeSelected: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("RecipeBoxScreen", "ON_CREATE")
    }

    val viewModel = containerViewModel { RecipeBoxViewModel(it.recipeRepository) }
    val uiState by viewModel.state.collectAsState()

    var isSearchVisible by remember { mutableStateOf(false) }

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

                IconButton(onClick = {
                    isSearchVisible = !isSearchVisible
                    if (isSearchVisible) ForkfulLogger.logAction("RECIPE_BOX", "Search opened")
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ForestGreen
                    )
                }
            }

            if (isSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(PureWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(23.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.height(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchInput,
                        textStyle = TextStyle(fontSize = 14.sp, color = TextCharcoal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(text = "Search your recipes...", color = TextMuted, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    RecipeBoxFilter.ALL to "All",
                    RecipeBoxFilter.SAVED to "Saved",
                    RecipeBoxFilter.COOKED to "Cooked"
                ).forEach { (filter, label) ->
                    val isTabSelected = uiState.filter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isTabSelected) ForestGreen else PureWhite)
                            .border(
                                width = 1.dp,
                                color = if (isTabSelected) ForestGreen else BorderLight,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setFilter(filter) }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isTabSelected) PureWhite else TextCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> LoadingState("Opening your Recipe Box...")
                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::load
                )
                uiState.displayedRecipes.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (uiState.filter) {
                            RecipeBoxFilter.COOKED -> "Nothing cooked yet.\nSwipe right to start collecting recipes."
                            RecipeBoxFilter.SAVED -> "No saved recipes yet.\nYour right-swipes will land here."
                            else -> "Your Recipe Box is empty.\nSwipe right on recipes you love."
                        },
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.displayedRecipes) { recipe ->
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
                    else -> recipe.category
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
