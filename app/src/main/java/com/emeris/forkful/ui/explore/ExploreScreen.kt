package com.emeris.forkful.ui.explore

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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.emeris.forkful.core.designsystem.AlertRed
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

data class CategoryAvatar(val name: String, val colorBg: Color)

@Composable
fun ExploreScreen(
    onNavigateTo: (String) -> Unit,
    onRecipeSelected: (String) -> Unit,
    onStackSelected: () -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("ExploreScreen", "ON_CREATE")
    }

    val categories = listOf(
        CategoryAvatar("Italian", Color(0xFFCBEAD7)),
        CategoryAvatar("Asian", Color(0xFFD7EAE4)),
        CategoryAvatar("Mexican", Color(0xFFFFE1E6)),
        CategoryAvatar("Braai", Color(0xFFC75A66)),
        CategoryAvatar("Vegan", Color(0xFFCDEFE5))
    )

    Scaffold(
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.Explore.route,
                onNavigateTo = onNavigateTo
            )
        },
        containerColor = CreamBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Good evening,",
                                fontSize = 15.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "What's for dinner?",
                                fontFamily = FontFamily.Serif,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextCharcoal
                            )
                        }
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { onNavigateTo(Screen.Notifications.route) }) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = TextCharcoal,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp, end = 10.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AlertRed)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFFF1EDE4))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search recipes, ingredients...",
                            color = TextMuted,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = TextCharcoal
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        categories.forEach { cat ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(cat.colorBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (cat.name == "Braai") PureWhite else ForestGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 12.sp,
                                    color = TextCharcoal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Prep time v", "Rating v", "Difficulty v", "Under 30m")) { filter ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                                    .background(PureWhite)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 13.sp,
                                    color = TextCharcoal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Matches for you",
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            color = TextCharcoal
                        )
                        Text(
                            text = "See all",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen,
                            modifier = Modifier.clickable { onNavigateTo(Screen.RecipeBox.route) }
                        )
                    }
                }
            }

            items(MockData.sampleRecipes.take(2)) { recipe ->
                RecipeMatchCard(
                    recipe = recipe,
                    onClick = { onRecipeSelected(recipe.id) }
                )
            }

            item {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)) {
                    Text(
                        text = "Your mood today",
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        color = TextCharcoal
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MoodCard(
                            title = "Italian night",
                            subtitle = "12 perfect matches",
                            imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?q=80&w=600&auto=format&fit=crop",
                            onClick = onStackSelected
                        )
                    }
                    item {
                        MoodCard(
                            title = "Use up your pantry",
                            subtitle = "Based on your stock",
                            imageUrl = "https://images.unsplash.com/photo-1584473457406-6240486418e9?q=80&w=600&auto=format&fit=crop",
                            onClick = { onNavigateTo(Screen.Pantry.route) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RecipeMatchCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextCharcoal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD9534F),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.rating}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Text(
                        text = " · ${recipe.prepTimeMinutes} min",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MintLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${recipe.matchPercentage}% MATCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, BorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
fun MoodCard(
    title: String,
    subtitle: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = PureWhite.copy(alpha = 0.85f)
            )
        }
    }
}