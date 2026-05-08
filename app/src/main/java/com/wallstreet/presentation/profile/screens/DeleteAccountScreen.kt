package com.wallstreet.presentation.profile.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wallstreet.R
import com.wallstreet.presentation.profile.ProfileViewModel
import com.wallstreet.presentation.profile.components.ConfirmationDialog
import com.wallstreet.presentation.profile.components.DeleteReasonOption
import com.wallstreet.ui.theme.BorderColors
import org.koin.androidx.compose.koinViewModel

enum class DeleteReason(val title: String) {
    NOT_USING_ANYMORE("I don't use the app anymore"),
    FRESH_START("I want to start fresh with new data"),
    PRIVACY_CONCERNS("Privacy concerns"),
    SWITCHING_APP("Switching to another app"),
    TOO_MANY_BUGS("Too many bugs / technical issues"),
    OTHER("Other")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
    onDelete: () -> Unit
) {
    var selectedReason by remember { mutableStateOf<DeleteReason?>(null) }
    var text by remember { mutableStateOf<String>("") }
    var showDeleteAccontDialog by remember { mutableStateOf<Boolean>(false) }
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteUiState.Success) {
            onDelete()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delete Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier

                    .padding(horizontal = 12.dp)

            ) {
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier

                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.terms),
                        contentDescription = null
                    )
                    Text("This will permanently delete all your trades, journal entries, and analytics. This cannot be undone.")

                }
                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    "Why are you leaving? ",
                    style = MaterialTheme.typography.titleMedium
                )

                DeleteReason.entries.forEach { reason ->
                    DeleteReasonOption(
                        reason = reason,
                        selected = selectedReason == reason,
                        onSelect = { selectedReason = reason }
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            "Tell us more (optional)...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    maxLines = 5,
                    singleLine = false,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BorderColors.current.primary,
                        unfocusedBorderColor = BorderColors.current.secondary

                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedButton(
                    onClick = { showDeleteAccontDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = deleteState !is DeleteUiState.Loading,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = "delete account",
                        modifier = Modifier
                            .size(18.dp)


                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Delete Account")
                }


            }



            ConfirmationDialog(
                show = showDeleteAccontDialog,
                title = "Delete Account",
                message = "This will permanently delete your account. This cannot be undone.",
                confirmText = "Delete",
                isDestructive = true,
                icon = painterResource(id = R.drawable.delete),

                onConfirm = {
                    showDeleteAccontDialog = false
                    viewModel.deleteAccount()
                },

                onDismiss = {
                    showDeleteAccontDialog = false
                }
            )
        }

    }

}