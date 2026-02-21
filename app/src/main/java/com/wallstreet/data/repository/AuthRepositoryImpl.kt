package com.wallstreet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.constants.AppConstants
import com.wallstreet.core.result.Result
import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.Success(result.user!!.toUserModel())
    } catch (e: Exception) {
        Result.Error(e.friendlyMessage(), e)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user!!.toUserModel()
        saveUserToFirestore(user)
        Result.Success(user)
    } catch (e: Exception) {
        Result.Error(e.friendlyMessage(), e)
    }

    override suspend fun signUp(fullName: String, email: String, password: String): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user!!
        firebaseUser.updateProfile(userProfileChangeRequest { displayName = fullName }).await()
        val user = User(
            id = firebaseUser.uid, name = fullName, email = email,
            photoUrl = TODO(),
//            createdAt = TODO()
        )
        saveUserToFirestore(user)
        Result.Success(user)
    } catch (e: Exception) {
        Result.Error(e.friendlyMessage(), e)
    }

    override suspend fun signOut() = auth.signOut()

    override fun getCurrentUser(): User? = auth.currentUser?.toUserModel()


    override fun observeAuthState(): Flow<User?> {
        TODO("Not yet implemented")
    }

    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection(AppConstants.COLLECTION_USERS)
            .document(user.id)
            .set(mapOf("id" to user.id, "name" to user.name,
                "email" to user.email, "photoUrl" to user.photoUrl))
            .await()
    }

    private fun FirebaseUser.toUserModel() = User(
        id = uid,
        name = displayName ?: "Trader",
        email = email ?: "",
        photoUrl = photoUrl?.toString(),
//        createdAt = TODO()
    )

    private fun Exception.friendlyMessage(): String = when {
        message?.contains("email address is already in use") == true ->
            "An account with this email already exists"
        message?.contains("password is invalid") == true ->
            "Incorrect password"
        message?.contains("no user record") == true ->
            "No account found with this email"
        message?.contains("network") == true ->
            "Network error. Please check your connection"
        else -> message ?: "An error occurred"
    }
}