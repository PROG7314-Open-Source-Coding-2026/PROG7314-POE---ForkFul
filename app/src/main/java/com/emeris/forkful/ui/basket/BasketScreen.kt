package com.emeris.forkful.ui.basket

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun BasketScreen(
    onNavigateTo: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("BasketScreen", "ON_CREATE")
    }

    val viewModel = containerViewModel { BasketViewModel(it.groceryRepository) }
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current

    val shareBasket: () -> Unit = {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "My Forkful basket")
            putExtra(Intent.EXTRA_TEXT, uiState.asShareText())
        }
        ForkfulLogger.logAction("BASKET", "Sharing basket (${uiState.totalItemCount} items)")
        context.startActivity(Intent.createChooser(sendIntent, "Share basket via"))
    }

    Scaffold(
        bottomBar = {
            ForkfulBottomBar(
                currentRoute = Screen.Basket.route,
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
                        text = "Basket",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        color = ForestGreen
                    )

                    IconButton(onClick = shareBasket) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = ForestGreen
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        LoadingState("Gathering your list...")
                    }
                }
                uiState.errorMessage != null -> item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        ErrorState(message = uiState.errorMessage.orEmpty(), onRetry = viewModel::load)
                    }
                }
                else -> {
                    item {
                        if (uiState.totalItemCount > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(ForestGreen.copy(alpha = 0.08f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "${uiState.checkedItemCount} of ${uiState.totalItemCount} items collected",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }

                    uiState.bannerText?.let { banner ->
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PureWhite)
                                    .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MintLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = ForestGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Smart Grocery Aggregator",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextCharcoal
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = banner,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = TextMuted
                                        )
                                    }

                                    IconButton(
                                        onClick = viewModel::dismissBanner,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    if (uiState.aisles.isEmpty() && !uiState.isLoading && uiState.errorMessage == null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your basket is empty.\nSave recipes and their missing\ningredients will appear here.",
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = TextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    uiState.aisles.forEach { aisle ->
                        item {
                            Text(
                                text = aisle.name.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PureWhite)
                                    .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                            ) {
                                Column {
                                    aisle.items.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setChecked(item.id, !item.isChecked)
                                                }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (item.isChecked) ForestGreen else Color(0xFFD3CEC4),
                                                        shape = CircleShape
                                                    )
                                                    .background(if (item.isChecked) ForestGreen else Color.Transparent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (item.isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = PureWhite,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column {
                                                Text(
                                                    text = item.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (item.isChecked) TextMuted else TextCharcoal
                                                )
                                                Text(
                                                    text = item.quantity,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = TextMuted
                                                )
                                            }
                                        }

                                        if (index < aisle.items.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color(0xFFF3EFE7))
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }
}
