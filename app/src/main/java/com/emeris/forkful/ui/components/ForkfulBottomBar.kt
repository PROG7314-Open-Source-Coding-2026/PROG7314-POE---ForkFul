package com.emeris.forkful.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AllInbox
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.i18n.currentStrings
import com.emeris.forkful.ui.navigation.Screen

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun ForkfulBottomBar(
    currentRoute: String,
    onNavigateTo: (String) -> Unit
) {
    val strings = currentStrings()
    val items = listOf(
        BottomNavItem(Screen.Explore.route, strings.navExplore, Icons.Outlined.Explore),
        BottomNavItem(Screen.RecipeBox.route, strings.navRecipeBox, Icons.Outlined.AutoStories),
        BottomNavItem(Screen.Pantry.route, strings.navPantry, Icons.Outlined.AllInbox),
        BottomNavItem(Screen.Basket.route, strings.navBasket, Icons.Outlined.ShoppingBasket),
        BottomNavItem(Screen.Settings.route, strings.navSettings, Icons.Outlined.Settings)
    )

    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomGapHeight = max(navBarBottomInset, 16.dp)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Soft top gradient fade into the navbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, PureWhite)
                    )
                )
        )

        // Main Navigation Bar Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderLight)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigateTo(item.route) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) ForestGreen else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) ForestGreen else TextMuted
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreen)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // Scaffold gap fade at the bottom (under the navbar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomGapHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PureWhite,
                            PureWhite.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

//------------------------------End-Of-File------------------------------//
