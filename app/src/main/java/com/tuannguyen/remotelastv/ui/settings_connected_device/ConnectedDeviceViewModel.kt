package com.tuannguyen.remotelastv.ui.settings_connected_device

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ConnectedDeviceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedTvRepository: SavedTvRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectedDeviceUiState())
    val uiState: StateFlow<ConnectedDeviceUiState> = _uiState.asStateFlow()

    init {
        onRetry()
    }

    fun onEditClick(tv: Television) {
        _uiState.update {
            it.copy(
                editingDevice = tv,
                editedName = tv.name,
                errorMessage = null,
            )
        }
    }

    fun onEditNameChange(value: String) {
        _uiState.update { it.copy(editedName = value) }
    }

    fun onConfirmRename() {
        val state = _uiState.value
        val television = state.editingDevice ?: return
        val newName = state.editedName.trim()
        if (newName.isEmpty()) return

        viewModelScope.launch {
            try {
                savedTvRepository.rename(television.id, newName)
                _uiState.update { currentState ->
                    currentState.copy(
                        devices = currentState.devices.map {
                            if (it.id == television.id) it.copy(name = newName) else it
                        },
                        editingDevice = null,
                        editedName = "",
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError(
                    context.getString(
                        R.string.connected_device_rename_failed,
                        exception.message ?: context.getString(R.string.error_unknown),
                    )
                )
            }
        }
    }

    fun onDismissEditDialog() {
        _uiState.update {
            it.copy(editingDevice = null, editedName = "")
        }
    }

    fun onDeleteClick(tv: Television) {
        _uiState.update {
            it.copy(deletingDevice = tv, errorMessage = null)
        }
    }

    fun onConfirmDelete() {
        val television = _uiState.value.deletingDevice ?: return

        viewModelScope.launch {
            try {
                savedTvRepository.delete(television.id)
                _uiState.update { state ->
                    state.copy(
                        devices = state.devices.filterNot { it.id == television.id },
                        deletingDevice = null,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError(
                    context.getString(
                        R.string.connected_device_remove_failed,
                        exception.message ?: context.getString(R.string.error_unknown),
                    )
                )
            }
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(deletingDevice = null) }
    }

    fun onRetry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val televisions = savedTvRepository.getTelevisions()
                _uiState.update {
                    it.copy(
                        devices = televisions,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError(
                    context.getString(
                        R.string.connected_device_load_failed,
                        exception.message ?: context.getString(R.string.error_unknown),
                    )
                )
            }
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}