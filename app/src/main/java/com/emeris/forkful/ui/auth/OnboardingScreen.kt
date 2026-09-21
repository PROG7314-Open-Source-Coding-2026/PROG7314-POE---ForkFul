package com.emeris.forkful.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.BorderLight
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.logging.ForkfulLogger

data class CraveCategory(
    val id: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("OnboardingScreen", "ON_CREATE")
    }

    val categories = listOf(
        CraveCategory("c1", "Italian", Icons.Default.LocalPizza),
        CraveCategory("c2", "Asian", Icons.Default.RamenDining),
        CraveCategory("c3", "Mexican", Icons.Default.TableBar),
        CraveCategory("c4", "Comfort", Icons.Default.BakeryDining),
        CraveCategory("c5", "Healthy", Icons.Default.Spa),
        CraveCategory("c6", "Sweets", Icons.Default.Cake)
    )

    val dietaryOptions = listOf("Vegetarian", "Halaal", "No shellfish", "Vegan", "Gluten-Free")

    val selectedCategories = remember { mutableStateListOf("c1", "c3", "c4") }
    val selectedDietary = remember { mutableStateListOf("Vegetarian", "No shellfish") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "What do you crave?",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            color = TextCharcoal,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Step 3 of 4: pick a few to seed your taste profiles",
            fontSize = 14.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(36.dp))

        for (rowIndex in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (colIndex in 0 until 3) {
                    val item = categories[rowIndex * 3 + colIndex]
                    val isSelected = selectedCategories.contains(item.id)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                if (isSelected) selectedCategories.remove(item.id)
                                else selectedCategories.add(item.id)
                                ForkfulLogger.logAction("ONBOARDING_CRAVE_TOGGLE", item.label)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ForestGreen else Color(0xFFE9E4DC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) PureWhite else Color(0xFF67645E),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) ForestGreen else TextMuted
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Any dietary needs?",
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            color = TextCharcoal
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            dietaryOptions.forEach { option ->
                val isDietSelected = selectedDietary.contains(option)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isDietSelected) ForestGreen else PureWhite)
                        .border(
                            width = 1.dp,
                            color = if (isDietSelected) ForestGreen else BorderLight,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable {
                            if (isDietSelected) selectedDietary.remove(option)
                            else selectedDietary.add(option)
                            ForkfulLogger.logAction("ONBOARDING_DIETARY_TOGGLE", option)
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = option,
                        color = if (isDietSelected) PureWhite else Color(0xFF4C524E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                ForkfulLogger.logAction("ONBOARDING", "Completed step to main flow")
                onCompleted()
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
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}