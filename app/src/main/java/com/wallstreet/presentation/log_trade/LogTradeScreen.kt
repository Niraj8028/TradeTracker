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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.core.util.formatDate
import com.wallstreet.domain.model.TradeType
import com.wallstreet.presentation.log_trade.components.ImageUploadSection
import com.wallstreet.presentation.log_trade.components.MistakesSection
import com.wallstreet.presentation.log_trade.components.PnlPreviewCard
import com.wallstreet.presentation.log_trade.components.StrategyDropdown
import com.wallstreet.presentation.log_trade.components.TradeTypeToggle
import com.wallstreet.ui.theme.White
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTradeScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: LogTradeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.tradeDate)
    var showDatePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    LaunchedEffect(uiState.success) {
        if(uiState.success) {
            onNavigateBack()
            viewModel.resetSuccess()
        }
    }

    val pnl = remember(uiState.entryPrice, uiState.exitPrice, uiState.quantity, uiState.tradeType) {
        val entry = uiState.entryPrice.toDoubleOrNull()
        val exit = uiState.exitPrice.toDoubleOrNull()
        val qty = uiState.quantity.toDoubleOrNull()
        if (entry != null && exit != null && qty != null && entry > 0 && exit > 0 && qty > 0) {
            val raw = (exit - entry) * qty
            if (uiState.tradeType == TradeType.LONG) raw else -raw
        } else null
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
//        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Log Trade",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = MaterialTheme.colorScheme.onBackground
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.background
//                )
//            )
        }
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Trade type toggle
                TradeTypeToggle(
                    selectedType = uiState.tradeType,
                    onTypeSelected = { viewModel.onTradeTypeChanged(it) }
                )

                // Live P&L preview
                if (pnl != null) {
                    PnlPreviewCard(pnl = pnl)
                }

                // Trade Details
                SectionCard(label = "Trade Details") {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppTextField(
                            value = uiState.entryPrice,
                            onValueChange = { viewModel.onEntryPriceChanged(it) },
                            label = "ENTRY PRICE",
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
                            label = "EXIT PRICE",
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

                    AppTextField(
                        value = uiState.stopLoss ?: "",
                        onValueChange = { viewModel.onStopLossChanged(it) },
                        label = "STOP LOSS (OPTIONAL)",
                        placeholder = "185.00",
                        prefix = "$",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Strategy
                SectionCard(label = "Strategy") {
                    StrategyDropdown(
                        selectedStrategy = uiState.selectedStrategy,
                        strategies = uiState.strategies,
                        onStrategySelected = { viewModel.onStrategySelected(it) }
                    )
                }

                // Trade Date
                SectionCard(label = "Trade Date") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = formatDate(uiState.tradeDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Mistakes
                if (viewModel.mistakes.isNotEmpty()) {
                    SectionCard(label = "Mistakes Identified") {
                        MistakesSection(
                            selectedMistakes = uiState.selectedMistakes,
                            mistakes = viewModel.mistakes,
                            onMistakeToggled = { viewModel.onMistakeToggled(it) }
                        )
                    }
                }

                // Notes & Screenshot
                SectionCard(label = "Notes & Screenshot") {
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
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorContainerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )

                    ImageUploadSection(
                        imageUri = uiState.imageUri,
                        onImagePick = { imagePickerLauncher.launch("image/*") }
                    )
                }

                // Error banner
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            // Date picker dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                            showDatePicker = false
                        }) { Text("OK") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Save button — pinned at the bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
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
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, shape, ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.15f))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), shape)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = if (prefix != null) {
                {
                    Text(
                        prefix,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
