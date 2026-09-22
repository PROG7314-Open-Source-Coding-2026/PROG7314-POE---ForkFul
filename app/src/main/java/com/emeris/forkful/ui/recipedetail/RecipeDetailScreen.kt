package com.emeris.forkful.ui.recipedetail

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
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.MintLight
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.logging.ForkfulLogger
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
    LaunchedEffect(recipeId) {
        ForkfulLogger.logLifecycle("RecipeDetailScreen", "ID: $recipeId")
    }

    val viewModel = containerViewModel { RecipeDetailViewModel(it.recipeRepository) }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(recipeId) {
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
            uiState.isLoading -> LoadingState("Plating your recipe...")
            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage.orEmpty(),
                onRetry = { viewModel.load(recipeId) }
            )
            else -> {
                val recipe = uiState.recipe ?: return@Scaffold
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = recipe.category,
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            Text(
                                text = "${recipe.totalIngredientsCount} ingredients",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextCharcoal
                            )
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            AsyncImage(
                                model = recipe.imageUrl,
                                contentDescription = recipe.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                                    .background(PureWhite)
                                    .padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .width(42.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFE2DDD2))
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = recipe.title,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = TextCharcoal,
                                        modifier = Modifier.weight(1f)
                                    )

                                    val saveClickModifier = if (recipe.isSaved) {
                                        Modifier
                                    } else {
                                        Modifier.clickable { viewModel.toggleSave() }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, BorderLight, CircleShape)
                                            .then(saveClickModifier),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (recipe.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Save",
                                            tint = if (recipe.isSaved) Color(0xFFD9534F) else TextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MintLight)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "Pantry: ${recipe.inPantryCount} of ${recipe.totalIngredientsCount} in your pantry",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ForestGreen
                                        )
                                    }
                                    if (recipe.isCooked) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFE8F0E9))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "Cooked",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ForestGreen
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "CAL: ${recipe.calories} KCAL",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextCharcoal
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderLight))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "PRO: ${recipe.proteinGrams}G PRO",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextCharcoal
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderLight))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "TIME: ${recipe.prepTimeMinutes} MIN",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextCharcoal
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Ingredients",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = TextCharcoal
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Items you already have are highlighted",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }

                        items(recipe.ingredients) { ingredient ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (ingredient.inPantry) Color(0xFFF1F6EF) else Color(0xFFF9F6F0))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (ingredient.inPantry) MintLight else Color(0xFFECE7DC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (ingredient.inPantry) ForestGreen else TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = ingredient.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = TextCharcoal
                                        )
                                        Text(
                                            text = ingredient.quantity,
                                            fontSize = 13.sp,
                                            color = TextMuted
                                        )
                                    }

                                    if (ingredient.inPantry) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "IN PANTRY",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                Button(
                                    onClick = {
                                        ForkfulLogger.logAction("COOKING_SESSION", "Started for ${recipe.title}")
                                        onStartCooking(recipe.id)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(27.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForestGreen,
                                        contentColor = PureWhite
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (recipe.isCooked) "Cook again" else "Start cooking",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }
}
