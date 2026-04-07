package com.wallstreet.presentation.log_trade

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.core.util.formatDate
import com.wallstreet.domain.model.TradeType
import com.wallstreet.presentation.log_trade.components.ImageUploadSection
import com.wallstreet.presentation.log_trade.components.MistakesSection
import com.wallstreet.presentation.log_trade.components.StrategyDropdown
import com.wallstreet.presentation.log_trade.components.TradeTypeToggle
import com.wallstreet.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTradeScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: LogTradeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.tradeDate
    )
    val scrollState = rememberScrollState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    // Live P&L calculation
    val pnl = remember(uiState.entryPrice, uiState.exitPrice, uiState.quantity) {
        val entry = uiState.entryPrice.toDoubleOrNull()
        val exit = uiState.exitPrice.toDoubleOrNull()
        val qty = uiState.quantity.toDoubleOrNull()
        if (entry != null && exit != null && qty != null) {
            val raw = (exit - entry) * qty
            val isLong = uiState.tradeType == TradeType.LONG
            if (isLong) raw else -raw
        } else null
    }
    var showDatePicker by remember { mutableStateOf(false) }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )
            {

                // ── Trade Type Toggle ──────────────────────────────
                TradeTypeToggle(
                    selectedType = uiState.tradeType,
                    onTypeSelected = { viewModel.onTradeTypeChanged(it) }
                )

                // ── P&L Preview Card (shows when calculable) ───────
                if (pnl != null) {
                    PnlPreviewCard(pnl = pnl)
                }


                // ── Section: Trade Details ─────────────────────────
                SectionCard() {
                    // Ticker
                    AppTextField(
                        value = uiState.symbol,
                        onValueChange = { viewModel.onSymbolChanged(it) },
                        label = "TICKER SYMBOL",
                        placeholder = "AAPL",
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        isError = uiState.symbolError != null,
                        errorMessage = uiState.symbolError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quantity
                    AppTextField(
                        value = uiState.quantity,
                        onValueChange = { viewModel.onQuantityChanged(it) },
                        label = "QUANTITY",
                        placeholder = "100",
                        isError = uiState.quantityError != null,
                        errorMessage = uiState.quantityError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Entry / Exit row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppTextField(
                            value = uiState.entryPrice,
                            onValueChange = { viewModel.onEntryPriceChanged(it) },
                            label = "ENTRY",
                            placeholder = "189.45",
                            prefix = "$",
                            isError = uiState.entryPriceError != null,
                            errorMessage = uiState.entryPriceError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        AppTextField(
                            value = uiState.exitPrice,
                            onValueChange = { viewModel.onExitPriceChanged(it) },
                            label = "EXIT",
                            placeholder = "192.10",
                            prefix = "$",
                            isError = uiState.exitPriceError != null,
                            errorMessage = uiState.exitPriceError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Stop Loss (optional)
                    AppTextField(
                        value = uiState.stopLoss ?: "",
                        onValueChange = { viewModel.onStopLossChanged(it) },
                        label = "STOP LOSS (OPTIONAL)",
                        placeholder = "185.00",
                        prefix = "$",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                // ── Section: Strategy ──────────────────────────────
                SectionCard() {
                    StrategyDropdown(
                        selectedStrategy = uiState.selectedStrategy,
                        strategies = uiState.strategies,
                        onStrategySelected = { viewModel.onStrategySelected(it) }
                    )
                }
//-- Section : Trade date
                SectionCard {
                    Text(
                        text = "TRADE DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formatDate(uiState.tradeDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showDatePicker = true }
                            .padding(16.dp)
                    )
                }

                // ── Section: Mistakes ──────────────────────────────
                if (viewModel.mistakes.isNotEmpty()) {
                    SectionCard() {
                        MistakesSection(
                            selectedMistakes = uiState.selectedMistakes,
                            mistakes = viewModel.mistakes,
                            onMistakeToggled = { viewModel.onMistakeToggled(it) }
                        )
                    }
                }

                // ── Section: Notes & Screenshot ────────────────────
                SectionCard() {
                    OutlinedTextField(
                        value = uiState.comments,
                        onValueChange = { viewModel.onCommentsAdded(it) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        placeholder = {
                            Text(
                                "What went well? What would you do differently?",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,

                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

                            cursorColor = MaterialTheme.colorScheme.primary,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )

                    ImageUploadSection(
                        imageUri = uiState.imageUri,
                        onImagePick = { imagePickerLauncher.launch("image/*") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                viewModel.onDateChange(it)
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                    )
                }
            }
            // ── Save Button ────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,

                ) {
                Button(
                    onClick = { viewModel.onSaveTrade() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save Trade",
                            style = MaterialTheme.typography.titleMedium,
                            color = White
                        )
                    }
                }
            }
        }
    }
}

// ── Reusable Section Card ──────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 0.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        content()
    }
}

// ── Reusable Text Field ────────────────────────────────────────────────────────

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    prefix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            placeholder = {
                Text(
                    placeholder,
                    color = DarkTextTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = if (prefix != null) {
                {
                    Text(
                        prefix,
                        color = DarkTextTertiary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else null,
            trailingIcon = trailingIcon,
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, style = MaterialTheme.typography.labelSmall) }
            } else null,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,

                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

                cursorColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// ── P&L Preview Card ───────────────────────────────────────────────────────────

@Composable
private fun PnlPreviewCard(pnl: Double) {
    val isProfit = pnl >= 0
    val bgColor =
        if (isProfit)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)

    val textColor =
        if (isProfit)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.error
    val label = if (isProfit) "ESTIMATED PROFIT" else "ESTIMATED LOSS"
    val sign = if (isProfit) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = 0.8f),
            letterSpacing = 0.8.sp
        )
        Text(
            text = "$sign$${"%.2f".format(pnl)}",
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}