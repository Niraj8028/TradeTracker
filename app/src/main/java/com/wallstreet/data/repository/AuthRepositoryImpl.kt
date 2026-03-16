package com.wallstreet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.wallstreet.core.constants.AppConstants
import com.wallstreet.core.result.AuthState
import com.wallstreet.core.result.Result
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.model.UserDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.domain.model.User
import com.wallstreet.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            if (!firebaseUser.isEmailVerified) {
                auth.signOut() // kick them out immediately
                return Result.Error("Please verify your email before logging in.")
            }

            Result.Success(firebaseUser.toUserModel())
        } catch (e: Exception) {
            Result.Error(e.friendlyMessage(), e)
        }
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

    override suspend fun signUp(fullName: String, email: String, password: String): Result<User> =
        try {
            //1 create account
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!
            firebaseUser.updateProfile(userProfileChangeRequest { displayName = fullName }).await()
            val user = User(id = firebaseUser.uid, name = fullName, email = email)
            //save User to fire store
            verifyEmail(firebaseUser)

            saveUserToFirestore(user)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.friendlyMessage(), e)
        }

    private suspend fun verifyEmail(firebaseUser: FirebaseUser) {
        firebaseUser.sendEmailVerification().await()
    }


    override suspend fun verifyEmail(): Result<Boolean> = try {
        auth.currentUser?.reload()?.await()
        val isVerified = auth.currentUser?.isEmailVerified ?: false
        Result.Success(isVerified)
    } catch (e: Exception) {
        Timber.e(e, e.friendlyMessage())
        Result.Error(e.friendlyMessage(), e)

    }

    override suspend fun signOut() = auth.signOut()
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.friendlyMessage(), e)
    }


    override fun getCurrentUser(): User? = auth.currentUser?.toUserModel()


    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection(AppConstants.COLLECTION_USERS)
            .document(user.id)
            .set(
                mapOf(
                    "id" to user.id, "name" to user.name,
                    "email" to user.email, "photoUrl" to user.photoUrl
                )
            )
            .await()
    }

    private fun FirebaseUser.toUserModel() = User(
        id = uid,
        name = displayName ?: "Trader",
        email = email ?: "",
        photoUrl = photoUrl?.toString()
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



    // TODO user this reactive method in navigation
    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser;
            if(firebaseUser != null){
                firestore.collection(FirebaseService.Collections.USERS)
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if(error != null) {
                            trySend(AuthState.UnAuthenticated)
                            return@addSnapshotListener
                        }

                        val user = snapshot?.toObject(UserDto::class.java)?.toDomain()
                        if (user!=null) {
                            trySend(AuthState.Authenticated(user))
                        } else {
                            trySend(AuthState.UnAuthenticated)
                        }
                    }
            } else {
                trySend(AuthState.UnAuthenticated)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener)}
    }
}