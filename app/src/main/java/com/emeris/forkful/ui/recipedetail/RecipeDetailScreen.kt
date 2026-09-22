package com.emeris.forkful.ui.recipedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.emeris.forkful.core.designsystem.AccentGreen
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.ButtonGreen
import com.emeris.forkful.core.designsystem.CardBorder
import com.emeris.forkful.core.designsystem.CardSurface
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.PantryBadgeBg
import com.emeris.forkful.core.designsystem.PantryBadgeText
import com.emeris.forkful.core.designsystem.SubtleCircleBg
import com.emeris.forkful.core.designsystem.SubtleCircleIcon
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.designsystem.TrueWhite
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.Ingredient
import com.emeris.forkful.ui.components.ErrorState
import com.emeris.forkful.ui.components.ForkfulBottomBar
import com.emeris.forkful.ui.components.LoadingState
import com.emeris.forkful.ui.di.containerViewModel
import com.emeris.forkful.ui.navigation.Screen

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onClose: () -> Unit,
    onNavigateTo: (String) -> Unit,
    onStartCooking: (String) -> Unit
) {
    val viewModel = containerViewModel { RecipeDetailViewModel(it.recipeRepository) }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(recipeId) {
        ForkfulLogger.logLifecycle("RecipeDetailScreen", "ID: $recipeId")
        viewModel.load(recipeId)
    }

    Scaffold(
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.Explore.route,
                onNavigateTo = onNavigateTo
            )
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
                    LoadingState("Loading recipe...")
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
                        onRetry = { viewModel.load(recipeId) }
                    )
                }
            }
            uiState.recipe != null -> {
                val recipe = uiState.recipe!!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .background(CardSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (recipe.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = recipe.imageUrl,
                                        contentDescription = recipe.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.RestaurantMenu,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = recipe.category,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 16.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = recipe.category,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGreen
                                        )
                                        Text(
                                            text = "${recipe.ingredients.size} ingredients",
                                            fontSize = 13.sp,
                                            color = TextMuted
                                        )
                                    }
                                    IconButton(
                                        onClick = onClose,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.45f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = TrueWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = recipe.title,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 28.sp,
                                        color = TextCharcoal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.toggleSave() },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(CardSurface)
                                            .border(1.dp, CardBorder, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (recipe.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Save recipe",
                                            tint = if (recipe.isSaved) Color(0xFFE57373) else TextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PantryBadgeBg)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Pantry: ${recipe.inPantryCount} of ${recipe.totalIngredientsCount} in your pantry",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PantryBadgeText
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CAL:  ${recipe.calories} KCAL",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal
                                    )
                                    Text(text = "|", color = BorderLight)
                                    Text(
                                        text = "PRO:  ${recipe.proteinGrams}G PRO",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal
                                    )
                                    Text(text = "|", color = BorderLight)
                                    Text(
                                        text = "TIME:  ${recipe.prepTimeMinutes} MIN",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Ingredients",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 22.sp,
                                    color = TextCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Items you already have are highlighted",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }

                        items(recipe.ingredients) { ingredient ->
                            IngredientRowItem(ingredient = ingredient)
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(CreamBackground.copy(alpha = 0.95f))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { onStartCooking(recipe.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonGreen,
                                contentColor = TrueWhite
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = TrueWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start cooking",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientRowItem(ingredient: Ingredient) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SubtleCircleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SubtleCircleIcon,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ingredient.quantity,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }
    }
}