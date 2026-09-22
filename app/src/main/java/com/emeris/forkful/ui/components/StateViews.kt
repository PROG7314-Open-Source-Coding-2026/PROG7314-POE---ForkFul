package com.emeris.forkful.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted

//Loading view
@Composable
fun LoadingState(message: String = "Loading...") {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = ForestGreen,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = message, color = TextMuted, fontSize = 14.sp)
    }
}

//Error view
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextCharcoal
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                contentColor = PureWhite
            )
        ) {
            Text(text = "Try again")
        }
    }
}
