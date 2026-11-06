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
import androidx.compose.material.icons.filled.Check
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
import com.emeris.forkful.domain.model.PantryItem

/**
 * Forkful - Pantry Item Management Bottom Sheet
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Interactive bottom sheet enabling users to add or edit inventory items within their digital pantry.
 * Supports item naming, amount/unit specification, category taxonomy assignment, and shelf-life tracking.
 *
 * @param sheetState Bottom sheet animation and expansion state.
 * @param onDismiss Invoked when the user closes or dismisses the bottom sheet.
 * @param onConfirm Invoked with sanitized values when the user saves or updates the item.
 * @param editingItem If present, pre-populates fields to edit an existing pantry entity; null for new items.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPantryItemBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    // daysUntilExpiry is null only when editing and the expiry is left unchanged
    onConfirm: (name: String, quantity: String, category: String, daysUntilExpiry: Int?) -> Unit,
    editingItem: PantryItem? = null
) {
    val isEditing = editingItem != null
    val categories = listOf(
        "Produce", "Dairy", "Butchery", "Bakery",
        "Pantry", "Dry Goods", "Frozen", "Spices", "Condiments"
    )
    val focusManager = LocalFocusManager.current
    var name by remember(editingItem) { mutableStateOf(editingItem?.name.orEmpty()) }
    val initialAmount = remember(editingItem) {
        val rawQty = editingItem?.quantity
        if (!rawQty.isNullOrBlank()) {
            rawQty.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
        } else {
            val label = editingItem?.quantityLabel.orEmpty().trim()
            val firstPart = label.split(Regex("\\s+")).firstOrNull().orEmpty()
            firstPart.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
        }
    }
    val initialUnit = remember(editingItem) {
        val rawUnit = editingItem?.unit
        if (!rawUnit.isNullOrBlank()) {
            rawUnit
        } else {
            val label = editingItem?.quantityLabel.orEmpty().trim()
            val parts = label.split(Regex("\\s+"), limit = 2)
            if (parts.size > 1 && parts[1].isNotBlank()) parts[1].trim() else "items"
        }
    }
    var quantityAmount by remember(editingItem) { mutableStateOf(initialAmount) }
    var selectedUnit by remember(editingItem) { mutableStateOf(initialUnit) }
    var selectedCategory by remember(editingItem) {
        mutableStateOf(editingItem?.category?.takeIf { it in categories } ?: "Produce")
    }
    var selectedDays by remember(editingItem) { mutableStateOf<Int?>(if (isEditing) null else 7) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
        val cleanAmount = quantityAmount.trim()
        val cleanUnit = selectedUnit.trim()
        val combinedQuantity = when {
            cleanAmount.isNotBlank() && cleanUnit.isNotBlank() -> "$cleanAmount $cleanUnit"
            cleanAmount.isNotBlank() -> "$cleanAmount items"
            cleanUnit.isNotBlank() -> "1 $cleanUnit"
            else -> "1 item"
        }
        validationError = null
        onConfirm(
            name.trim(),
            combinedQuantity,
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
                        text = if (isEditing) "Edit pantry item" else "Add to Pantry",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextCharcoal
                    )
                    Text(
                        text = if (isEditing) {
                            "Update the name, amount or shelf life"
                        } else {
                            "Track your stock to unlock recipe matches"
                        },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = quantityAmount,
                    onValueChange = { input ->
                        // Only allow numeric input (digits and at most one decimal point). Strictly reject letters.
                        val filtered = input.replace(',', '.').filter { it.isDigit() || it == '.' }
                        val dotCount = filtered.count { it == '.' }
                        if (dotCount <= 1) {
                            quantityAmount = filtered.take(8)
                        }
                    },
                    label = { Text("Quantity") },
                    placeholder = { Text("1") },
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
                    placeholder = { Text("items") },
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
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val commonUnits = listOf("items", "g", "kg", "ml", "L", "tins", "packs")
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
            if (isEditing && selectedDays == null) {
                Text(
                    text = "Keeping the current expiry - pick an option to change it",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
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
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = TrueWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Save changes" else "Add to Pantry",
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

//------------------------------End-Of-File------------------------------//
