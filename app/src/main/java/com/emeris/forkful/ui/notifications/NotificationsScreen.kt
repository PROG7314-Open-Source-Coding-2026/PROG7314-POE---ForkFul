package com.emeris.forkful.ui.notifications

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.data.repository.MockData
import com.emeris.forkful.domain.model.NotificationModel
import com.emeris.forkful.domain.model.NotificationType

@Composable
fun NotificationsScreen(
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("NotificationsScreen", "ON_CREATE")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "9:41",
            fontFamily = FontFamily.Serif,
            fontSize = 62.sp,
            fontWeight = FontWeight.Normal,
            color = TextCharcoal
        )

        Text(
            text = "TUESDAY, OCTOBER 24",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(PureWhite)
                .border(1.dp, BorderLight, RoundedCornerShape(22.dp))
                .clickable { onDismiss() }
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ForestGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Fork",
                                fontSize = 12.sp,
                                color = PureWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FORKFUL",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = "now",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Spinach expires today",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap for 3 fast, vibrant recipes that use it up before it wilts.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Recent",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                color = TextCharcoal
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(MockData.sampleNotifications) { notification ->
                RecentNotificationCard(notification = notification)
            }
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