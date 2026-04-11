package com.wallstreet.data.store

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.wallstreet.data.mapper.toDomain
import com.wallstreet.data.model.TradeDto
import com.wallstreet.data.remote.FirebaseService
import com.wallstreet.domain.model.Trade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TradeStore(private val firestore: FirebaseFirestore) {

    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    val trades: StateFlow<List<Trade>> = _trades.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    fun startObserving(userId: String) {
        if (listenerRegistration != null) return
        _isLoading.value = true

        listenerRegistration = firestore
            .collection(FirebaseService.Collections.TRADES)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                _trades.value = snapshot?.documents?.mapNotNull {
                    it.toObject(TradeDto::class.java)?.toDomain()
                } ?: emptyList()
                _isLoading.value = false
            }
    }

    fun stopObserving() {
        listenerRegistration?.remove()
        listenerRegistration = null
        _trades.value = emptyList()
        _isLoading.value = true
    }
}