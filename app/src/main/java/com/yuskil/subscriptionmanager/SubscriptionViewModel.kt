package com.yuskil.subscriptionmanager

import android.app.Application
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object Failed : SaveState
    data class Saved(val id: Long) : SaveState
}

internal class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SubscriptionRepository(SubscriptionDatabase.get(application).subscriptions())
    val subscriptions = repository.subscriptions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val mutableSaveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = mutableSaveState.asStateFlow()

    fun save(id: Long?, fields: SubscriptionFields) {
        val current = mutableSaveState.value
        if (current != SaveState.Idle && current != SaveState.Failed) return
        mutableSaveState.value = SaveState.Saving
        viewModelScope.launch {
            mutableSaveState.value = try {
                repository.save(id, fields)?.let { SaveState.Saved(it) } ?: SaveState.Failed
            } catch (error: SQLiteException) {
                Log.e("SubscriptionViewModel", "Failed to save subscription", error)
                SaveState.Failed
            }
        }
    }

    fun clearSaveState() {
        if (mutableSaveState.value != SaveState.Saving) {
            mutableSaveState.value = SaveState.Idle
        }
    }
}
