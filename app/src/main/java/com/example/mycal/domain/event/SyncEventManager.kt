package com.example.mycal.domain.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEventManager @Inject constructor() {
    private val _syncCompletedEvent = MutableSharedFlow<Unit>()
    val syncCompletedEvent = _syncCompletedEvent.asSharedFlow()

    suspend fun notifySyncCompleted() {
        _syncCompletedEvent.emit(Unit)
    }
}