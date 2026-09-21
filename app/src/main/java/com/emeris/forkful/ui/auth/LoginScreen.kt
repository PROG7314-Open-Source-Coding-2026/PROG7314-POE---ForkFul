package com.emeris.forkful.ui.auth

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.DarkAuthBackground
import com.emeris.forkful.core.designsystem.DarkAuthBorder
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.logging.ForkfulLogger

@Composable
fun ForkfulLogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(ForestGreen),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(44.dp)) {
            val strokeW = 4f
            val forkX = size.width * 0.35f
            val knifeX = size.width * 0.65f

            drawLine(Color.White, Offset(forkX - 8f, 10f), Offset(forkX - 8f, 32f), strokeW)
            drawLine(Color.White, Offset(forkX, 10f), Offset(forkX, 32f), strokeW)
            drawLine(Color.White, Offset(forkX + 8f, 10f), Offset(forkX + 8f, 32f), strokeW)
            drawLine(Color.White, Offset(forkX - 8f, 32f), Offset(forkX + 8f, 32f), strokeW)
            drawLine(Color.White, Offset(forkX, 32f), Offset(forkX, size.height - 10f), strokeW + 2f)

            val knifePath = Path().apply {
                moveTo(knifeX, 10f)
                cubicTo(knifeX + 16f, 12f, knifeX + 16f, 34f, knifeX, 40f)
                close()
            }
            drawPath(knifePath, Color.White)
            drawLine(Color.White, Offset(knifeX, 38f), Offset(knifeX, size.height - 10f), strokeW + 2f)
        }
    }
}

@Composable
fun LoginScreen(
    onContinueWithGoogle: () -> Unit,
    onContinueWithEmail: () -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("LoginScreen", "ON_CREATE")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAuthBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1.0f))

        ForkfulLogoBadge()

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Forkful",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            color = Color(0xFFC7EED8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Swipe your way to your next\nfavourite meal.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = Color(0xFFB5BEB7),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1.2f))

        Button(
            onClick = {
                ForkfulLogger.logAction("LOGIN", "Continue with Google tapped")
                onContinueWithGoogle()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = Color(0xFF1E211E)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "G",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(Color(0xFF171A16))
                .border(1.dp, DarkAuthBorder, RoundedCornerShape(27.dp))
                .clickable {
                    ForkfulLogger.logAction("LOGIN", "Continue with Email tapped")
                    onContinueWithEmail()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Email",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "By continuing, you agree to our Terms of Service\nand Privacy Policy.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Color(0xFF6B736D),
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}