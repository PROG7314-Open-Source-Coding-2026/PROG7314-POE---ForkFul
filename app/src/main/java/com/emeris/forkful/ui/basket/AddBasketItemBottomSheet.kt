package com.emeris.forkful.ui.basket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emeris.forkful.core.designsystem.AlertPinkText
import com.emeris.forkful.core.designsystem.ButtonGreen
import com.emeris.forkful.core.designsystem.CardBorder
import com.emeris.forkful.core.designsystem.CardSurface
import com.emeris.forkful.core.designsystem.CreamBackground
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.MintLight
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.designsystem.SubtleCircleBg
import com.emeris.forkful.core.designsystem.SubtleCircleIcon
import com.emeris.forkful.core.designsystem.TextCharcoal
import com.emeris.forkful.core.designsystem.TextMuted
import com.emeris.forkful.core.designsystem.TrueWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBasketItemBottomSheet(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var quantityAmount by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf("items") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val staples = listOf("Milk", "Eggs", "Bread", "Butter", "Olive Oil", "Coffee", "Bananas", "Garlic", "Onions", "Cheese")
    val commonUnits = listOf("items", "tins", "packs", "g", "kg", "ml", "L", "bunch")

    fun submit() {
        if (name.isBlank()) {
            validationError = "Please enter an item name"
            return
        }
        val cleanAmount = quantityAmount.trim()
        val cleanUnit = selectedUnit.trim()
        val combinedQuantity = when {
            cleanAmount.isNotBlank() && cleanUnit.isNotBlank() -> "$cleanAmount $cleanUnit"
            cleanAmount.isNotBlank() -> "$cleanAmount items"
            cleanUnit.isNotBlank() -> "1 $cleanUnit"
            else -> "1 item"
        }
        validationError = null
        onConfirm(name.trim(), combinedQuantity)
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
                        text = "Add to Basket",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextCharcoal
                    )
                    Text(
                        text = "Track items you need on your next grocery run",
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Quick Staples",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(staples) { staple ->
                    val isSelected = name.equals(staple, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ForestGreen else CardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(if (isSelected) MintLight else PureWhite)
                            .clickable {
                                name = staple
                                validationError = null
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = staple,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) ForestGreen else TextCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(80)
                    validationError = null
                },
                label = { Text("Item name (e.g. Sourdough bread, Oat milk)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = SubtleCircleIcon
                    )
                },
                trailingIcon = if (name.isNotBlank()) {
                    {
                        IconButton(onClick = { name = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                isError = validationError != null,
                supportingText = validationError?.let {
                    { Text(it, color = AlertPinkText) }
                },
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

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Amount & Unit",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantityAmount,
                    onValueChange = { input ->
                        quantityAmount = input.replace(',', '.').filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Quantity") },
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
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Right) }
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = { selectedUnit = it.take(20) },
                    label = { Text("Unit") },
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
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commonUnits.forEach { unitOption ->
                    val isUnitActive = selectedUnit.equals(unitOption, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = if (isUnitActive) ForestGreen else CardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (isUnitActive) MintLight else PureWhite)
                            .clickable { selectedUnit = unitOption }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unitOption,
                            fontSize = 11.sp,
                            fontWeight = if (isUnitActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isUnitActive) ForestGreen else TextCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = { submit() },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonGreen,
                    contentColor = TrueWhite,
                    disabledContainerColor = ButtonGreen.copy(alpha = 0.5f),
                    disabledContentColor = TrueWhite.copy(alpha = 0.7f)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TrueWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adding to basket...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueWhite
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to basket",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

//------------------------------End-Of-File------------------------------//
