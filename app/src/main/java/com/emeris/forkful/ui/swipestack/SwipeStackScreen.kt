package com.emeris.forkful.ui.swipestack

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.ui.components.ErrorState
import com.emeris.forkful.ui.components.LoadingState
import com.emeris.forkful.ui.di.containerViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeStackScreen(
    moodKey: String,
    onDismiss: () -> Unit,
    onInspectRecipe: (String) -> Unit
) {
    LaunchedEffect(moodKey) {
        ForkfulLogger.logLifecycle("SwipeStackScreen", "mood=$moodKey")
    }

    val viewModel = containerViewModel { SwipeViewModel(it.recipeRepository) }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(moodKey) {
        viewModel.loadDeck(moodKey)
    }

    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val moodLabel = moodKey.replaceFirstChar { it.uppercase() } + if (moodKey == "pantry") " stack" else " night"

    fun performSwipe(direction: SwipeDirection) {
        coroutineScope.launch {
            val targetX = if (direction == SwipeDirection.RIGHT) 1200f else -1200f
            offsetX.animateTo(targetX, tween(250))
            viewModel.swipe(direction)
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = moodLabel,
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
                Text(
                    text = uiState.stackLabel,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextCharcoal
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingState("Building your stack...")
            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage.orEmpty(),
                onRetry = viewModel::reload
            )
            uiState.currentRecipe == null -> StackExhaustedState(
                modifier = Modifier.weight(1f),
                sessionCount = uiState.sessionCount,
                onReload = viewModel::reload,
                onClose = onDismiss
            )
            else -> {
                val recipe = uiState.currentRecipe

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                        .graphicsLayer {
                            rotationZ = (offsetX.value / 40f)
                        }
                        .pointerInput(recipe?.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        when {
                                            offsetX.value > 250f -> performSwipe(SwipeDirection.RIGHT)
                                            offsetX.value < -250f -> performSwipe(SwipeDirection.LEFT)
                                            offsetY.value < -350f -> {
                                                recipe?.let { onInspectRecipe(it.id) }
                                                offsetY.animateTo(0f, tween(200))
                                            }
                                            else -> {
                                                offsetX.animateTo(0f, tween(200))
                                                offsetY.animateTo(0f, tween(200))
                                            }
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount.x)
                                        offsetY.snapTo(offsetY.value + dragAmount.y)
                                    }
                                }
                            )
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(PureWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            AsyncImage(
                                model = recipe?.imageUrl.orEmpty(),
                                contentDescription = recipe?.title.orEmpty(),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PureWhite.copy(alpha = 0.9f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Clock: ${recipe?.prepTimeMinutes ?: 0} min",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextCharcoal
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MintLight.copy(alpha = 0.95f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Pantry: ${recipe?.inPantryCount ?: 0} in stock",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = recipe?.title.orEmpty(),
                                fontFamily = FontFamily.Serif,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextCharcoal
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = recipe?.description.orEmpty(),
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF5EFE6))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE2DDD2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Poll,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "WHY THIS IS ON TOP",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = recipe?.tags?.joinToString(", ")?.ifBlank { "Fresh pick for you" }
                                                .orEmpty(),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ForestGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PureWhite)
                            .border(1.dp, BorderLight, CircleShape)
                            .clickable { performSwipe(SwipeDirection.LEFT) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reject",
                            tint = ForestGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ForestGreen)
                            .clickable { recipe?.let { onInspectRecipe(it.id) } },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Details",
                            tint = PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MintLight)
                            .clickable { performSwipe(SwipeDirection.RIGHT) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Accept",
                            tint = ForestGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StackExhaustedState(
    modifier: Modifier = Modifier,
    sessionCount: Int,
    onReload: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Stack complete!",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                color = TextCharcoal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You rated $sessionCount dishes. Your taste profile\nhas been updated for the next round.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(ForestGreen)
                        .clickable { onReload() }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(text = "Deal again", color = PureWhite, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                        .background(PureWhite)
                        .clickable { onClose() }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(text = "Done", color = TextCharcoal, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
