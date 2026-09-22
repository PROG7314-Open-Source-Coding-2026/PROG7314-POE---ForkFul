package com.emeris.forkful.ui.pantry

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.ButtonGreen
import com.emeris.forkful.core.designsystem.CardBorder
import com.emeris.forkful.core.designsystem.CardSurface
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.MintLight
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.SubtleCircleBg
import com.emeris.forkful.core.designsystem.SubtleCircleIcon
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.designsystem.TrueWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPantryItemBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: String, category: String, daysUntilExpiry: Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Produce") }
    var selectedDays by remember { mutableIntStateOf(7) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Produce", "Dairy", "Butchery", "Bakery",
        "Pantry", "Dry Goods", "Frozen", "Spices", "Condiments"
    )
    val expiryOptions = listOf(
        Pair("3 days", 3),
        Pair("1 week", 7),
        Pair("2 weeks", 14),
        Pair("1 month", 30),
        Pair("3 months", 90)
    )

    fun submit() {
        if (name.isBlank()) {
            validationError = "Please enter an item name."
            return
        }
        validationError = null
        onConfirm(
            name.trim(),
            quantity.trim().ifBlank { "1 piece" },
            selectedCategory,
            selectedDays
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SubtleCircleIcon.copy(alpha = 0.4f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add to Pantry",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextCharcoal
                    )
                    Text(
                        text = "Track your stock to unlock recipe matches",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SubtleCircleBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = SubtleCircleIcon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                },
                label = { Text("Item name (e.g. Baby spinach, Salmon)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = SubtleCircleIcon
                    )
                },
                singleLine = true,
                isError = validationError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCharcoal,
                    unfocusedTextColor = TextCharcoal,
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = CardBorder,
                    focusedLabelColor = ForestGreen,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = ForestGreen
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (validationError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = validationError.orEmpty(),
                    color = Color(0xFFE5989B),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity (e.g. 500 g, 2 tins, 1 bunch)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCharcoal,
                    unfocusedTextColor = TextCharcoal,
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = CardBorder,
                    focusedLabelColor = ForestGreen,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = ForestGreen
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Aisle Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextCharcoal
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ForestGreen else CardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(if (isSelected) ForestGreen else PureWhite)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) TrueWhite else TextCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Estimated Shelf Life",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextCharcoal
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                expiryOptions.forEach { (label, days) ->
                    val isSelected = selectedDays == days
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ForestGreen else CardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (isSelected) MintLight else PureWhite)
                            .clickable { selectedDays = days }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ForestGreen else TextCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonGreen,
                    contentColor = TrueWhite
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = TrueWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Pantry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}