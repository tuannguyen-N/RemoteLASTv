package com.tuannguyen.remotelastv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.manager.TvTextInputManager
import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val textInputManager: TvTextInputManager,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private sealed interface TextCommand {
        data class Send(val text: String) : TextCommand
        data class Delete(val count: Int) : TextCommand
        data object Enter : TextCommand
    }

    private val isAutoShowKeyboardEnabled: StateFlow<Boolean> = settingsRepository.remoteSettings
        .map { it.isAutoShowKeyboardEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val sheetVisible = MutableStateFlow(false)
    private val query = MutableStateFlow("")
    private val commands = Channel<TextCommand>(Channel.UNLIMITED)
    private val pending = AtomicInteger(0)

    private val tvInput: StateFlow<TextInputState> = textInputManager.inputState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TextInputState.Unknown)

    private fun latestTvText(): String? = (tvInput.value as? TextInputState.Focused)?.text

    val uiState: StateFlow<KeyboardUiState> = combine(
        sheetVisible,
        query,
        tvInput,
        textInputManager.capabilities,
    ) { visible, q, input, caps ->
        KeyboardUiState(
            query = q,
            input = input,
            canObserveFocus = TextCapability.OBSERVE_FOCUS in caps,
            showKeyboardSheet = visible,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KeyboardUiState())

    init {
        viewModelScope.launch {
            tvInput
                .filter { it !is TextInputState.Unknown }
                .map { it is TextInputState.Focused }
                .distinctUntilChanged()
                .collect { isFocused ->
                    if (isFocused) {
                        query.value = latestTvText() ?: ""
                        if (isAutoShowKeyboardEnabled.value) sheetVisible.value = true
                    } else {
                        sheetVisible.value = false
                        query.value = ""
                    }
                }
        }

        viewModelScope.launch {
            tvInput
                .filterIsInstance<TextInputState.Focused>()
                .mapNotNull { it.text }
                .distinctUntilChanged()
                .collect { text ->
                    if (pending.get() == 0 && text != query.value) query.value = text
                }
        }

        viewModelScope.launch {
            for (cmd in commands) {
                try {
                    process(cmd)
                } finally {
                    pending.decrementAndGet()
                }
            }
        }
    }

    private suspend fun process(cmd: TextCommand) {
        when (cmd) {
            is TextCommand.Send -> textInputManager.execute { it.sendText(cmd.text) }
            is TextCommand.Delete -> repeat(cmd.count) {
                textInputManager.execute { it.sendDelete() }
            }

            TextCommand.Enter -> textInputManager.execute { it.sendEnter() }
        }
    }

    private fun enqueue(cmd: TextCommand) {
        pending.incrementAndGet()
        if (!commands.trySend(cmd).isSuccess) pending.decrementAndGet()
    }

    fun onQueryChange(new: String) {
        val old = query.value
        if (new == old) return
        query.value = new

        val common = old.commonPrefixWith(new).length
        val toDelete = old.length - common
        val toSend = new.substring(common)

        if (toDelete > 0) enqueue(TextCommand.Delete(toDelete))
        if (toSend.isNotEmpty()) enqueue(TextCommand.Send(toSend))
    }

    fun onEnter() = enqueue(TextCommand.Enter)

    fun showKeyboardSheet() {
        query.value = latestTvText() ?: ""
        sheetVisible.value = true
    }

    fun dismissKeyboardSheet() {
        sheetVisible.value = false
        query.value = ""
    }
}