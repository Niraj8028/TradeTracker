package com.wallstreet.presentation.profile

import androidx.lifecycle.ViewModel
import com.wallstreet.domain.model.User
import com.wallstreet.domain.usecase.auth.GetCurrentUserUseCase

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    val user: User? = getCurrentUserUseCase()
}