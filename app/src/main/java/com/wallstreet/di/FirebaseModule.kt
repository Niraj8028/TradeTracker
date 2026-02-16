package com.wallstreet.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val firebaseModule = module {
    // Firebase Auth - Singleton
    single { FirebaseAuth.getInstance() }

    // Firestore - Singleton
    single { FirebaseFirestore.getInstance() }
}