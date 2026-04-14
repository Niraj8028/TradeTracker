package com.wallstreet.presentation.profile.screens


sealed class DeleteUiState {

    object Idle : DeleteUiState()

    object Loading : DeleteUiState()

    object Success : DeleteUiState()

    data class Error(
        val message: String
    ) : DeleteUiState()
}