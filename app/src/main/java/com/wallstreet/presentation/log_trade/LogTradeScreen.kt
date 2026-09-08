package com.wallstreet.presentation.log_trade

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.core.util.HapticStyle
import com.wallstreet.core.util.format
import com.wallstreet.core.util.formatPnl
import com.wallstreet.core.util.hapticClickable
import com.wallstreet.ui.theme.LocalCurrencySymbol
import com.wallstreet.domain.model.Strategy
import com.wallstreet.domain.model.TradeType
import com.wallstreet.domain.model.TrendDirection
import com.wallstreet.presentation.log_trade.components.FlowRow
import com.wallstreet.presentation.log_trade.components.StrategyDropdown
import com.wallstreet.ui.theme.DangerRed
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.SuccessGreen
import com.wallstreet.ui.theme.White
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val TrendAmber = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTradeScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = onNavigateBack,
    viewModel: LogTradeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.tradeDate)
    var showDatePicker by remember { mutableStateOf(false) }

    val datePick = remember(uiState.tradeDate) {
        val tradeDate = Instant.ofEpochMilli(uiState.tradeDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        when (tradeDate) {
            today -> "today"
            today.minusDays(1) -> "yesterday"
            else -> "custom"
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateToHome()
            viewModel.resetSuccess()
        }
    }

    // LiveCalc derived values
    val entryVal = uiState.entryPrice.toDoubleOrNull()
    val exitVal  = uiState.exitPrice.toDoubleOrNull()
    val qtyVal   = uiState.quantity.toDoubleOrNull()
    val stopVal  = uiState.stopLoss?.toDoubleOrNull()
    val isLong   = uiState.tradeType == TradeType.LONG

    val pnl: Double? = if (entryVal != null && exitVal != null && qtyVal != null)
        (if (isLong) (exitVal - entryVal) else (entryVal - exitVal)) * qtyVal
    else null

    val pctReturn: Double? = if (entryVal != null && exitVal != null && entryVal > 0)
        (if (isLong) ((exitVal - entryVal) / entryVal) else ((entryVal - exitVal) / entryVal)) * 100
    else null

    val rrRatio: Double? = if (entryVal != null && exitVal != null && stopVal != null) {
        val reward = abs(exitVal - entryVal)
        val risk   = if (isLong) abs(entryVal - stopVal) else abs(stopVal - entryVal)
        if (risk > 0) reward / risk else null
    } else null

    val customDateLabel = remember(uiState.tradeDate) {
        Instant.ofEpochMilli(uiState.tradeDate)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d MMM"))
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .hapticClickable(HapticStyle.Light) { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Log Trade",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Scrollable body ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Card 1: Trade ────────────────────────────────────────────
                TradeCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FieldBlock(label = "Symbol", modifier = Modifier.weight(1.6f)) {
                            TradeInput(
                                value = uiState.symbol,
                                onChange = { viewModel.onSymbolChanged(it) },
                                placeholder = "AAPL",
                                isError = uiState.symbolError != null,
                                trailingContent = {
                                    Icon(Icons.Default.Search, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp))
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                            if (uiState.symbolError != null) {
                                Text(uiState.symbolError!!, fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp))
                            }
                        }
                        FieldBlock(label = "Qty", hint = "shares", modifier = Modifier.weight(1f)) {
                            TradeInput(
                                value = uiState.quantity,
                                onChange = { viewModel.onQuantityChanged(it) },
                                placeholder = "100",
                                isError = uiState.quantityError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                )
                            )
                            if (uiState.quantityError != null) {
                                Text(uiState.quantityError!!, fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp))
                            }
                        }
                    }

                    FieldBlock(label = "Direction") {
                        DirectionSegment(
                            selected = uiState.tradeType,
                            onSelect = { viewModel.onTradeTypeChanged(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FieldBlock(label = "Entry", modifier = Modifier.weight(1f)) {
                            TradeInput(
                                value = uiState.entryPrice,
                                onChange = { viewModel.onEntryPriceChanged(it) },
                                placeholder = "0.00",
                                prefix = LocalCurrencySymbol.current,
                                isError = uiState.entryPriceError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                )
                            )
                            if (uiState.entryPriceError != null) {
                                Text(uiState.entryPriceError!!, fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp))
                            }
                        }
                        FieldBlock(label = "Exit", modifier = Modifier.weight(1f)) {
                            TradeInput(
                                value = uiState.exitPrice,
                                onChange = { viewModel.onExitPriceChanged(it) },
                                placeholder = "0.00",
                                prefix = LocalCurrencySymbol.current,
                                isError = uiState.exitPriceError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                )
                            )
                            if (uiState.exitPriceError != null) {
                                Text(uiState.exitPriceError!!, fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp))
                            }
                        }
                    }

                    FieldBlock(label = "Stop loss", optional = true) {
                        TradeInput(
                            value = uiState.stopLoss ?: "",
                            onChange = { viewModel.onStopLossChanged(it) },
                            placeholder = "0.00",
                            prefix = LocalCurrencySymbol.current,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    LiveCalcStrip(pnl = pnl, pctReturn = pctReturn, rrRatio = rrRatio)
                }

                // ── Card 2: Context ──────────────────────────────────────────
                TradeCard {
                    FieldBlock(label = "Strategy") {
                        StrategyDropdown(
                            selectedStrategy = uiState.selectedStrategy,
                            strategies = uiState.strategies,
                            onStrategySelected = { viewModel.onStrategySelected(it) }
                        )
                    }

                    FieldBlock(label = "Market trend", optional = true) {
                        TrendSegment(
                            selected = uiState.trendDirection,
                            onSelect = { viewModel.onTrendDirectionSelected(it) }
                        )
                    }

                    FieldBlock(label = "Trade date") {
                        DateChipRow(
                            selected = datePick,
                            customLabel = customDateLabel,
                            onSelect = { chip ->
                                when (chip) {
                                    "today" -> viewModel.onDateChange(System.currentTimeMillis())
                                    "yesterday" -> viewModel.onDateChange(System.currentTimeMillis() - 86_400_000L)
                                    "custom" -> showDatePicker = true
                                }
                            }
                        )
                    }
                }

                // ── Card 3: Mistakes ─────────────────────────────────────────
                TradeCard {
                    FieldBlock(
                        label = "Mistakes",
                        optional = true
                    ) {
                        MistakeChipSet(
                            selected = uiState.selectedMistakes,
                            mistakes = viewModel.mistakes,
                            onToggle = { viewModel.onMistakeToggled(it) }
                        )
                    }
                }

                // ── Card 4: Notes + Screenshot ───────────────────────────────
                TradeCard {
                    FieldBlock(
                        label = "Notes",
                        optional = true,
                        hint = "${uiState.comments.length} / 500"
                    ) {
                        NotesTextArea(
                            value = uiState.comments,
                            onChange = { if (it.length <= 500) viewModel.onCommentsAdded(it) }
                        )
                    }

                    FieldBlock(label = "Screenshot", optional = true) {
                        ScreenshotUploadButton(
                            uri = uiState.imageUri,
                            onPick = { imagePickerLauncher.launch("image/*") },
                            onClear = { viewModel.onImageSelected("") }
                        )
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // ── Sticky save bar ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = PrimaryBlue.copy(alpha = 0.35f),
                        spotColor = PrimaryBlue.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue)
                    .hapticClickable(HapticStyle.Medium, enabled = !uiState.isLoading) { viewModel.onSaveTrade() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(15.dp))
                        Text(
                            text = "Save Trade",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ── Card container ────────────────────────────────────────────────────────────

@Composable
private fun TradeCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.10f))
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

// ── Field label + content wrapper ────────────────────────────────────────────

@Composable
private fun FieldBlock(
    label: String,
    modifier: Modifier = Modifier,
    optional: Boolean = false,
    hint: String? = null,
    hintContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (optional) {
                    Text(
                        text = " · optional",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            when {
                hintContent != null -> hintContent()
                hint != null -> Text(
                    text = hint,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
        }
        content()
    }
}

// ── Text input ────────────────────────────────────────────────────────────────

@Composable
private fun TradeInput(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    prefix: String? = null,
    isError: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError  -> MaterialTheme.colorScheme.error
            isFocused -> PrimaryBlue
            else     -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        },
        animationSpec = tween(150),
        label = "border"
    )

    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(PrimaryBlue),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (prefix != null) {
                    Text(
                        text = prefix,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
                trailingContent?.invoke()
            }
        }
    )
}

// ── Direction segmented control ───────────────────────────────────────────────

@Composable
private fun DirectionSegment(
    selected: TradeType,
    onSelect: (TradeType) -> Unit
) {
    val options = listOf(
        Triple(TradeType.LONG, "Long", SuccessGreen),
        Triple(TradeType.SHORT, "Short", DangerRed)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { (type, label, color) ->
            val isActive = selected == type
            val icon = if (type == TradeType.LONG) Icons.Default.TrendingUp else Icons.Default.TrendingDown
            val bgColor by animateColorAsState(
                targetValue = if (isActive) color else Color.Transparent,
                animationSpec = tween(150), label = "seg_bg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isActive) White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(150), label = "seg_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .hapticClickable(HapticStyle.Light) { onSelect(type) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(icon, null, tint = contentColor, modifier = Modifier.size(13.dp))
                    Text(
                        text = label,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

// ── Trend segmented control ───────────────────────────────────────────────────

@Composable
private fun TrendSegment(
    selected: TrendDirection?,
    onSelect: (TrendDirection) -> Unit
) {
    data class TrendOpt(val dir: TrendDirection, val label: String, val icon: ImageVector, val color: Color)

    val options = listOf(
        TrendOpt(TrendDirection.UP, "Up", Icons.Default.ArrowUpward, SuccessGreen),
        TrendOpt(TrendDirection.SIDEWAYS, "Sideways", Icons.Default.ArrowForward, TrendAmber),
        TrendOpt(TrendDirection.DOWN, "Down", Icons.Default.ArrowDownward, DangerRed)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { opt ->
            val isActive = selected == opt.dir
            val bgColor by animateColorAsState(
                targetValue = if (isActive) opt.color else Color.Transparent,
                animationSpec = tween(150), label = "trend_bg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isActive) White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(150), label = "trend_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .hapticClickable(HapticStyle.Light) {
                        if (selected == opt.dir) return@hapticClickable
                        onSelect(opt.dir)
                    }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(opt.icon, null, tint = contentColor, modifier = Modifier.size(14.dp))
                    Text(
                        text = opt.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

// ── Live Calc strip ───────────────────────────────────────────────────────────

@Composable
private fun LiveCalcStrip(pnl: Double?, pctReturn: Double?, rrRatio: Double?) {
    val bgColor = when {
        pnl == null -> MaterialTheme.colorScheme.surfaceVariant
        pnl >= 0    -> SuccessGreen.copy(alpha = 0.12f)
        else        -> DangerRed.copy(alpha = 0.12f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalcCell(
            label = "P&L",
            value = if (pnl == null) "—" else pnl.formatPnl(LocalCurrencySymbol.current),
            color = when {
                pnl == null -> MaterialTheme.colorScheme.onSurfaceVariant
                pnl >= 0    -> SuccessGreen
                else        -> DangerRed
            },
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .width(1.dp)
                .height(28.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        )
        CalcCell(
            label = "RETURN",
            value = if (pctReturn == null) "—"
                    else "${if (pctReturn >= 0) "+" else ""}${String.format(Locale.US, "%.2f", pctReturn)}%",
            color = when {
                pctReturn == null -> MaterialTheme.colorScheme.onSurfaceVariant
                pctReturn >= 0    -> SuccessGreen
                else              -> DangerRed
            },
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .width(1.dp)
                .height(28.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        )
        CalcCell(
            label = "R:R",
            value = if (rrRatio == null) "—" else "1 : ${rrRatio.format(2)}",
            color = if (rrRatio == null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CalcCell(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Date chip row ─────────────────────────────────────────────────────────────

@Composable
private fun DateChipRow(
    selected: String,
    customLabel: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf("today" to "Today", "yesterday" to "Yest.").forEach { (key, label) ->
            val isActive = selected == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isActive) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.5.dp,
                        if (isActive) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        RoundedCornerShape(9.dp)
                    )
                    .hapticClickable(HapticStyle.Light) { onSelect(key) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Custom date chip — grows to fill remaining space
        val isCustom = selected == "custom"
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isCustom) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.5.dp,
                    if (isCustom) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    RoundedCornerShape(9.dp)
                )
                .hapticClickable(HapticStyle.Light) { onSelect("custom") }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CalendarMonth, null,
                tint = if (isCustom) White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = customLabel,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCustom) White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Mistake chips ─────────────────────────────────────────────────────────────

@Composable
private fun MistakeChipSet(
    selected: Set<String>,
    mistakes: List<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        mistakes.forEach { mistake ->
            val isActive = selected.contains(mistake)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isActive) DangerRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.5.dp,
                        if (isActive) DangerRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        RoundedCornerShape(999.dp)
                    )
                    .hapticClickable(HapticStyle.Light) { onToggle(mistake) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (isActive) {
                    Icon(Icons.Default.Check, null, tint = DangerRed, modifier = Modifier.size(11.dp))
                }
                Text(
                    text = mistake,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Notes textarea ────────────────────────────────────────────────────────────

@Composable
private fun NotesTextArea(value: String, onChange: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        animationSpec = tween(150), label = "notes_border"
    )

    BasicTextField(
        value = value,
        onValueChange = onChange,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(PrimaryBlue),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .height(72.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "What worked? What would you change?",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        lineHeight = 20.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

// ── Screenshot upload ─────────────────────────────────────────────────────────

@Composable
private fun ScreenshotUploadButton(uri: String?, onPick: () -> Unit, onClear: () -> Unit) {
    if (uri != null && uri.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uri.substringAfterLast("/").take(32),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Image selected",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                )
                .hapticClickable(HapticStyle.Light) { onPick() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Image, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp))
                Text(
                    text = "Add screenshot",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
