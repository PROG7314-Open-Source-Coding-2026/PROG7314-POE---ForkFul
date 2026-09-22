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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.emeris.forkful.ui.di.containerViewModel

private val cuisineIcons: Map<String, ImageVector> = mapOf(
    "italian" to Icons.Default.LocalPizza,
    "asian" to Icons.Default.RamenDining,
    "mexican" to Icons.Default.TableBar,
    "braai" to Icons.Default.OutdoorGrill,
    "comfort" to Icons.Default.BakeryDining,
    "healthy" to Icons.Default.Spa,
    "sweets" to Icons.Default.Cake,
    "vegan" to Icons.Default.Eco
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("OnboardingScreen", "ON_CREATE")
    }

    val viewModel = containerViewModel { OnboardingViewModel(it.preferencesRepository) }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onCompleted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiState.step > 0) {
                IconButton(onClick = viewModel::previousStep) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextCharcoal
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Step ${uiState.step + 1} of 4",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (uiState.step + 1) / 4f },
            modifier = Modifier.fillMaxWidth(),
            color = ForestGreen,
            trackColor = Color(0xFFE9E4DC)
        )

        Spacer(modifier = Modifier.height(28.dp))

        when (uiState.step) {
            0 -> WelcomeStep(onStart = viewModel::nextStep)
            1 -> CravingsStep(
                state = uiState,
                onToggle = viewModel::toggleCuisine,
                onNext = viewModel::nextStep
            )
            2 -> DietaryStep(
                state = uiState,
                onToggle = viewModel::toggleDietary,
                onNext = viewModel::nextStep
            )
            else -> ReviewStep(
                state = uiState,
                onSubmit = viewModel::submit
            )
        }
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(48.dp))
        ForkfulLogoBadge()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome to Forkful",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            color = TextCharcoal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Answer a few quick questions so we can tune your recipe deck. Every swipe you make later keeps sharpening the recommendations.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        PrimaryButton(text = "Let's get started", onClick = onStart)
    }
}

@Composable
private fun CravingsStep(
    state: OnboardingUiState,
    onToggle: (String) -> Unit,
    onNext: () -> Unit
) {
    val choices = listOf(
        "italian", "asian", "mexican",
        "braai", "comfort", "healthy",
        "sweets", "vegan"
    )

    Text(
        text = "What do you crave?",
        fontFamily = FontFamily.Serif,
        fontSize = 32.sp,
        color = TextCharcoal
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Pick a few to seed your taste profiles",
        fontSize = 14.sp,
        color = TextMuted
    )
    Spacer(modifier = Modifier.height(32.dp))

    choices.chunked(3).forEach { rowChoices ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            rowChoices.forEach { key ->
                val label = key.replaceFirstChar { it.uppercase() }
                val icon = cuisineIcons[key] ?: Icons.Default.Spa
                val isSelected = key in state.selectedCuisines

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onToggle(key) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ForestGreen else Color(0xFFE9E4DC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) PureWhite else Color(0xFF67645E),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) ForestGreen else TextMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    Spacer(modifier = Modifier.height(8.dp))
    PrimaryButton(
        text = "Continue",
        enabled = state.canContinue,
        onClick = onNext
    )
    if (!state.canContinue) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose at least one craving to continue",
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DietaryStep(
    state: OnboardingUiState,
    onToggle: (String) -> Unit,
    onNext: () -> Unit
) {
    Text(
        text = "Any dietary needs?",
        fontFamily = FontFamily.Serif,
        fontSize = 28.sp,
        color = TextCharcoal
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Optional - these act as hard filters on every recipe deck",
        fontSize = 14.sp,
        color = TextMuted
    )
    Spacer(modifier = Modifier.height(28.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("Vegetarian", "Vegan", "Halaal", "No Shellfish", "Gluten-Free").forEach { option ->
            val isDietSelected = option in state.selectedDietary
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (isDietSelected) ForestGreen else PureWhite)
                    .border(
                        width = 1.dp,
                        color = if (isDietSelected) ForestGreen else BorderLight,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable { onToggle(option) }
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

    Spacer(modifier = Modifier.height(24.dp))
    PrimaryButton(text = "Continue", onClick = onNext)
}

@Composable
private fun ReviewStep(
    state: OnboardingUiState,
    onSubmit: () -> Unit
) {
    Text(
        text = "All set?",
        fontFamily = FontFamily.Serif,
        fontSize = 32.sp,
        color = TextCharcoal
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "We'll use this to rank your first swipe deck",
        fontSize = 14.sp,
        color = TextMuted
    )
    Spacer(modifier = Modifier.height(28.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PureWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "CRAVINGS",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.selectedCuisines.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } },
                fontSize = 15.sp,
                color = TextCharcoal
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "DIETARY FILTERS",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.selectedDietary.joinToString(", ").ifBlank { "None" },
                fontSize = 15.sp,
                color = TextCharcoal
            )
        }
    }

    state.errorMessage?.let { error ->
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = error,
            fontSize = 13.sp,
            color = com.emeris.forkful.core.designsystem.AlertPinkText
        )
    }

    Spacer(modifier = Modifier.height(28.dp))
    PrimaryButton(
        text = if (state.isSubmitting) "Saving..." else "Start swiping",
        enabled = !state.isSubmitting,
        onClick = onSubmit
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ForestGreen,
            contentColor = PureWhite,
            disabledContainerColor = Color(0xFFB9C9BF),
            disabledContentColor = PureWhite
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
