package com.example.mycal.presentation.screens.subscription

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycal.domain.model.CalendarSource
import com.example.mycal.domain.repository.CalendarSourceRepository
import com.example.mycal.domain.usecase.AddCalendarSourceUseCase
import com.example.mycal.domain.usecase.ValidateIcsUrlUseCase
import com.example.mycal.domain.usecase.ValidationResult
import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.domain.event.SyncEventManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val calendarSourceRepository: CalendarSourceRepository,
    private val addCalendarSourceUseCase: AddCalendarSourceUseCase,
    private val validateIcsUrlUseCase: ValidateIcsUrlUseCase,
    private val eventDao: EventDao,
    private val syncEventManager: SyncEventManager
) : ViewModel() {

    companion object {
        private const val TAG = "SubscriptionViewModel"
    }

    init {
        Log.d(TAG, "SubscriptionViewModel initialized")
        Log.d(TAG, "Repository: $calendarSourceRepository")
        Log.d(TAG, "AddUseCase: $addCalendarSourceUseCase")
        Log.d(TAG, "ValidateUseCase: $validateIcsUrlUseCase")
    }

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    val sources: StateFlow<List<CalendarSource>> = calendarSourceRepository
        .getAllSources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                addDialogState = AddSourceDialogState()
            )
        }
    }

    fun updateUrl(url: String) {
        _uiState.update { state ->
            val validation = validateIcsUrlUseCase(url)
            state.copy(
                addDialogState = state.addDialogState.copy(
                    url = url,
                    urlError = when (validation) {
                        is ValidationResult.Error -> validation.message
                        else -> null
                    }
                )
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { state ->
            state.copy(
                addDialogState = state.addDialogState.copy(name = name)
            )
        }
    }

    fun updateColor(color: Color) {
        _uiState.update { state ->
            state.copy(
                addDialogState = state.addDialogState.copy(color = color)
            )
        }
    }

    fun addSource() {
        val dialogState = _uiState.value.addDialogState
        val validation = validateIcsUrlUseCase(dialogState.url)

        if (validation !is ValidationResult.Success && validation !is ValidationResult.Warning) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            addCalendarSourceUseCase(
                url = dialogState.url,
                name = dialogState.name.ifBlank { "Calendar" },
                color = dialogState.color.toArgb()
            ).fold(
                onSuccess = {
                    hideAddDialog()
                    // Notify that sync is complete
                    syncEventManager.notifySyncCompleted()
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            addDialogState = state.addDialogState.copy(
                                error = error.message
                            )
                        )
                    }
                }
            )

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun deleteSource(sourceId: String) {
        viewModelScope.launch {
            calendarSourceRepository.deleteSource(sourceId)
        }
    }

    fun toggleSyncEnabled(sourceId: String, enabled: Boolean) {
        viewModelScope.launch {
            calendarSourceRepository.setSyncEnabled(sourceId, enabled)
        }
    }

    fun syncSource(sourceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncingSourceIds = it.syncingSourceIds + sourceId) }

            // Debug: Check event count before sync
            val beforeCount = eventDao.getEventCountBySource(sourceId)
            Log.d(TAG, "Events before sync for $sourceId: $beforeCount")

            calendarSourceRepository.syncSource(sourceId)

            // Give sync some time to complete
            kotlinx.coroutines.delay(2000)

            // Debug: Check event count after sync
            val afterCount = eventDao.getEventCountBySource(sourceId)
            Log.d(TAG, "Events after sync for $sourceId: $afterCount")

            // Debug: Get all events to check dates
            val allEvents = eventDao.getAllEvents()
            Log.d(TAG, "Total events in database: ${allEvents.size}")
            allEvents.filter { it.sourceId == sourceId }.take(5).forEach { event ->
                val startDate = java.time.Instant.ofEpochMilli(event.startTime)
                val endDate = java.time.Instant.ofEpochMilli(event.endTime)
                Log.d(TAG, "Event: ${event.title}, Start: $startDate, End: $endDate")
            }

            _uiState.update { it.copy(syncingSourceIds = it.syncingSourceIds - sourceId) }

            // Notify that sync is complete
            syncEventManager.notifySyncCompleted()
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingAll = true) }
            calendarSourceRepository.syncAllSources()
            _uiState.update { it.copy(isSyncingAll = false) }

            // Notify that sync is complete
            syncEventManager.notifySyncCompleted()
        }
    }
}

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val addDialogState: AddSourceDialogState = AddSourceDialogState(),
    val syncingSourceIds: Set<String> = emptySet(),
    val isSyncingAll: Boolean = false
)

data class AddSourceDialogState(
    val url: String = "",
    val name: String = "",
    val color: Color = Color.Blue,
    val urlError: String? = null,
    val error: String? = null
)