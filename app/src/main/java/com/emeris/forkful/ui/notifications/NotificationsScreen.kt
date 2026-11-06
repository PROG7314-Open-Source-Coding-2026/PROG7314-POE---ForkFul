package com.emeris.forkful.ui.notifications

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.emeris.forkful.core.i18n.currentStrings
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.notification.NotificationHelper
import com.emeris.forkful.domain.model.NotificationModel
import com.emeris.forkful.domain.model.NotificationType
import java.time.LocalTime

@Composable
fun NotificationsScreen(
    onDismiss: () -> Unit,
    onNavigateTo: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val strings = currentStrings()
    var lastTriggeredMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.showNotification(
                context = context,
                title = "Forkful Alert: Spinach Expiring!",
                body = "Your spinach expires today! Tap to view 3 quick recipes to use it up."
            )
            lastTriggeredMessage = "Real-time notification dispatched to device"
        }
    }

    fun dispatchNotification(title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasNotificationPermission(context)
        ) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationHelper.showNotification(context = context, title = title, body = body)
            lastTriggeredMessage = "Sent: \"$title\""
        }
    }

    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("NotificationsScreen", "ON_CREATE")
    }

    val sampleNotifications = remember {
        listOf(
            NotificationModel(
                id = "n1",
                title = "Action Required: Spinach Expiring Soon",
                description = "Your fresh spinach is nearing its expiry date in your pantry. Use it up in a Creamy Tuscan Chicken or a quick dinner bowl before it spoils.",
                timeAgo = "10m ago",
                type = NotificationType.ACTION_REQUIRED,
                isUnread = true
            ),
            NotificationModel(
                id = "n2",
                title = "Dinner Recommendation: Braaibroodjie",
                description = "You have all 5 ingredients (bread, cheddar, tomato, onion, chutney) in your pantry ready to grill tonight!",
                timeAgo = "1h ago",
                type = NotificationType.PANTRY_MATCH,
                isUnread = true
            ),
            NotificationModel(
                id = "n3",
                title = "Prep Reminder: Thaw Stewing Mutton",
                description = "Remember to take the mutton cubes out of the freezer early for tonight's slow-cooked Tomato Bredie.",
                timeAgo = "3h ago",
                type = NotificationType.PREP_REMINDER,
                isUnread = false
            ),
            NotificationModel(
                id = "n4",
                title = "Pantry Expiry Alert: Milk & Cream",
                description = "Dairy items nearing expiration date. Prepare a quick Malva Pudding or pan sauce to prevent food waste.",
                timeAgo = "5h ago",
                type = NotificationType.EXPIRY_ALERT,
                isUnread = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PureWhite)
                    .border(1.dp, BorderLight, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextCharcoal,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Notifications",
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )
                Text(
                    text = "Pantry alerts & smart meal recommendations",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FCM Connection & Status Pill
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintLight)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Firebase Cloud Messaging Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ForestGreen
                            )
                            Text(
                                text = "Real-time channel connected for automated pantry alerts",
                                fontSize = 11.sp,
                                color = TextCharcoal.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            // Quick Triggers Section (Real FCM / System Notifications)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PureWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Trigger Real-time Alerts",
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Test instant push notifications dispatched to your device's lockscreen and notification tray:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Pantry Expiry Alert
                    SmartTriggerButton(
                        icon = Icons.Default.Warning,
                        iconTint = AlertPinkText,
                        iconBg = AlertPinkBackground,
                        title = "1. Expiring Ingredients Alert",
                        subtitle = "Alerts user when items in pantry expire today/tomorrow",
                        buttonText = "Send Expiry Alert",
                        onClick = {
                            dispatchNotification(
                                title = "⚠️ Forkful Alert: Spinach Expiring Soon!",
                                body = "Your fresh spinach is nearing its expiry. Tap to view 3 quick recipes to use it up before it spoils!"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Meal Recommendation (Context-aware based on hour)
                    val currentHour = LocalTime.now().hour
                    val (mealTitle, mealRecipe, mealIngredients) = when {
                        currentHour < 11 -> Triple("Breakfast Idea", "Warm Pap & Chakalaka", "maize meal and baked beans")
                        currentHour < 15 -> Triple("Lunch Recommendation", "Peri-Peri Chicken Bowls", "chicken breasts, basmati rice, and sweetcorn")
                        else -> Triple("Dinner Recommendation", "Braaibroodjie", "bread, cheddar cheese, and tomatoes")
                    }

                    SmartTriggerButton(
                        icon = Icons.Default.DinnerDining,
                        iconTint = ForestGreen,
                        iconBg = MintLight,
                        title = "2. $mealTitle",
                        subtitle = "Recommends $mealRecipe using pending pantry stock",
                        buttonText = "Send Meal Alert",
                        onClick = {
                            dispatchNotification(
                                title = "🔥 $mealTitle: $mealRecipe",
                                body = "You have $mealIngredients in your pantry ready to cook tonight!"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. FCM Cloud Broadcast
                    SmartTriggerButton(
                        icon = Icons.Default.AutoAwesome,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        title = "3. Live FCM Cloud Broadcast",
                        subtitle = "Simulates broadcast notification pushed from cloud servers",
                        buttonText = "Send Cloud Alert",
                        onClick = {
                            dispatchNotification(
                                title = "⚡ Forkful Cloud Alert: Weekend Braai Special",
                                body = "New authentic Braai recipes have been added to your curated feed. Tap to explore!"
                            )
                        }
                    )

                    lastTriggeredMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MintLight)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓ $msg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            // Recent Alerts Feed Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Smart Alerts",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${sampleNotifications.size} active",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Recent Alerts Feed
            items(sampleNotifications) { notification ->
                RecentNotificationCard(notification = notification)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SmartTriggerButton(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CreamBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextCharcoal
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }
    }
}

@Composable
fun RecentNotificationCard(notification: NotificationModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PureWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            val (iconVector: ImageVector, bgTint: Color, iconTint: Color) = when (notification.type) {
                NotificationType.ACTION_REQUIRED -> Triple(Icons.Default.Warning, AlertPinkBackground, AlertPinkText)
                NotificationType.PREP_REMINDER -> Triple(Icons.Default.DinnerDining, MintLight, ForestGreen)
                NotificationType.PANTRY_MATCH -> Triple(Icons.Default.AutoAwesome, MintLight, ForestGreen)
                NotificationType.EXPIRY_ALERT -> Triple(Icons.Default.Warning, AlertPinkBackground, AlertPinkText)
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextCharcoal
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.timeAgo,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        if (notification.isUnread) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreen)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )
            }
        }
    }
}

//------------------------------End-Of-File------------------------------//
