package com.example.mycal.presentation.screens.subscription

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycal.domain.model.CalendarSource
import com.example.mycal.domain.repository.CalendarSourceRepository
import com.example.mycal.domain.usecase.AddCalendarSourceUseCase
import com.example.mycal.domain.usecase.ValidateIcsUrlUseCase
import com.example.mycal.domain.usecase.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val calendarSourceRepository: CalendarSourceRepository,
    private val addCalendarSourceUseCase: AddCalendarSourceUseCase,
    private val validateIcsUrlUseCase: ValidateIcsUrlUseCase
) : ViewModel() {

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
            calendarSourceRepository.syncSource(sourceId)
            _uiState.update { it.copy(syncingSourceIds = it.syncingSourceIds - sourceId) }
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingAll = true) }
            calendarSourceRepository.syncAllSources()
            _uiState.update { it.copy(isSyncingAll = false) }
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