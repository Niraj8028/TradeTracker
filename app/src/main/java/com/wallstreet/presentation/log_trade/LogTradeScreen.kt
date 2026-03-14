package com.wallstreet.presentation.log_trade

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wallstreet.presentation.log_trade.components.ImageUploadSection
import com.wallstreet.presentation.log_trade.components.MistakesSection
import com.wallstreet.presentation.log_trade.components.StrategyDropdown
import com.wallstreet.presentation.log_trade.components.TradeTypeToggle
import com.wallstreet.presentation.profile.ProfileViewModel
import com.wallstreet.ui.theme.DarkSurfaceVariant
import com.wallstreet.ui.theme.DarkTextPrimary
import com.wallstreet.ui.theme.DarkTextSecondary
import com.wallstreet.ui.theme.DarkTextTertiary
import com.wallstreet.ui.theme.PrimaryBlue
import com.wallstreet.ui.theme.White
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
fun LogTradeScreen(
//    onNavigationBack: () -> Unit,
//    onTradeLogged: () -> Unit
    viewModel: LogTradeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
            uri?.let { viewModel.onImageSelected(it.toString()) }
    }


    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(){
//
//        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TradeTypeToggle(
                    selectedType = uiState.tradeType,
                    onTypeSelected = { viewModel.onTradeTypeChanged(it) }
                )

                OutlinedTextField(
                    value = uiState.symbol,
                    onValueChange = { viewModel.onSymbolChanged(it) },
                    label = {
                        Text(
                            "TICKER SYMBOL",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkTextSecondary
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DarkTextSecondary
                        )
                    },
                    isError = uiState.symbolError != null,
                    supportingText = uiState.symbolError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.onQuantityChanged(it) },
                    label = {
                        Text(
                            "QUANTITY",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkTextSecondary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    placeholder = { Text("100", color = DarkTextTertiary) },
                    isError = uiState.quantityError != null,
                    supportingText = uiState.quantityError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.entryPrice,
                        onValueChange = { viewModel.onEntryPriceChanged(it) },
                        label = {
                            Text(
                                "ENTRY PRICE",
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkTextSecondary
                            )
                        },
                        leadingIcon = {
                            Text(
                                "$",
                                color = DarkTextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        placeholder = { Text("189.45", color = DarkTextTertiary) },
                        isError = uiState.entryPriceError != null,
                        supportingText = uiState.entryPriceError?.let { { Text(it, fontSize = 10.sp) } },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.exitPrice,
                        onValueChange = { viewModel.onExitPriceChanged(it) },
                        label = {
                            Text(
                                "EXIT PRICE",
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkTextSecondary
                            )
                        },
                        leadingIcon = {
                            Text(
                                "$",
                                color = DarkTextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        placeholder = { Text("192.10", color = DarkTextTertiary) },
                        isError = uiState.exitPriceError != null,
                        supportingText = uiState.exitPriceError?.let { { Text(it, fontSize = 10.sp) } },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Strategy Dropdown
                StrategyDropdown(
                    selectedStrategy = uiState.selectedStrategy,
                    strategies = viewModel.strategies,
                    onStrategySelected = { viewModel.onStrategySelected(it) }
                )

                MistakesSection(
                    selectedMistakes = uiState.selectedMistakes,
                    mistakes = viewModel.mistakes,
                    onMistakeToggled = { viewModel.onMistakeToggled(it) }
                )

                OutlinedTextField(
                    value = uiState.comments,
                    onValueChange = {
                        viewModel.onCommentsAdded(it)
                    },
//                    label = {
//                        Text(
//                            "Notes",
//                            style = MaterialTheme.typography.labelMedium,
//                            color = DarkTextSecondary
//                        )
//                    },
                    placeholder = {
                        Text("Add notes", color = DarkTextTertiary)
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                ImageUploadSection(
                    imageUri = uiState.imageUri,
                    onImagePick = {
                        imagePickerLauncher.launch("image/*")
                    }
                )
                Spacer(modifier = Modifier.height(80.dp))

            }

            Button(
                onClick = {
                    viewModel.onSaveTrade()
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = White
                        )
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )
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