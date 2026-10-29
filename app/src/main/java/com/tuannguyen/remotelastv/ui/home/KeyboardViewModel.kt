package com.tuannguyen.remotelastv.ui.home

import android.util.Log
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
        data class Replace(val text: String) : TextCommand
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

    private val capsSnapshot: StateFlow<Set<TextCapability>> = textInputManager.capabilities
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private fun latestTvText(): String? =
        (tvInput.value as? TextInputState.Focused)?.text?.toSingleLineText()

    val uiState: StateFlow<KeyboardUiState> = combine(
        sheetVisible,
        query,
        tvInput,
        textInputManager.capabilities,
    ) { visible, q, input, caps ->
        KeyboardUiState(
            query = q,
            input = input,
            inputHint = (input as? TextInputState.Focused)?.label,
            canObserveFocus = TextCapability.OBSERVE_FOCUS in caps,
            showKeyboardSheet = visible,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KeyboardUiState())

    init {
        viewModelScope.launch {
            tvInput
                .combine(textInputManager.capabilities) { input, capabilities ->
                    input to capabilities
                }
                .filter { (input, _) -> input !is TextInputState.Unknown }
                .map { (input, capabilities) ->
                    val focused = input as? TextInputState.Focused
                    Triple(
                        focused != null,
                        TextCapability.AUTO_SHOW_KEYBOARD_ON_FOCUS in capabilities &&
                                focused?.shouldAutoShowKeyboard == true,
                        focused?.label,
                    )
                }
                .distinctUntilChanged()
                .collect { (isFocused, autoShowOnFocus, label) ->
                    if (isFocused) {
                        query.value = latestTvText() ?: ""
                        if (autoShowOnFocus && isAutoShowKeyboardEnabled.value) {
                            setKeyboardSheetVisible(
                                true,
                                "TV text field ready for input (label=${label.orEmpty()})"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "TV text field focused; phone keyboard sheet not auto-shown " +
                                        "(label=${label.orEmpty()}, autoShow=$autoShowOnFocus, " +
                                        "setting=${isAutoShowKeyboardEnabled.value})"
                            )
                        }
                    } else {
                        setKeyboardSheetVisible(false, "TV text field is not focused")
                        query.value = ""
                    }
                }
        }

        viewModelScope.launch {
            textInputManager.keyboardShowRequests.collect {
                if (isAutoShowKeyboardEnabled.value) {
                    query.value = latestTvText() ?: ""
                    setKeyboardSheetVisible(true, "TV sent keyboard show request")
                } else {
                    Log.d(
                        TAG,
                        "TV sent keyboard show request; ignored because auto-show is disabled"
                    )
                }
            }
        }

        viewModelScope.launch {
            tvInput
                .filterIsInstance<TextInputState.Focused>()
                .mapNotNull { it.text }
                .map(String::toSingleLineText)
                .distinctUntilChanged()
                .collect { text ->
                    if (!sheetVisible.value && pending.get() == 0 && text != query.value) {
                        query.value = text
                    }
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
            is TextCommand.Replace -> textInputManager.execute { it.replaceText(cmd.text) }
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
        val normalized = new.toSingleLineText()
        val old = query.value
        if (normalized == old) return
        query.value = normalized

        val canReplace = TextCapability.REPLACE_TEXT in capsSnapshot.value
        if (canReplace) {
            enqueue(TextCommand.Replace(normalized))
        } else {
            val common = old.commonPrefixWith(normalized).length
            val toDelete = old.length - common
            val toSend = normalized.substring(common)

            if (toDelete > 0) enqueue(TextCommand.Delete(toDelete))
            if (toSend.isNotEmpty()) enqueue(TextCommand.Send(toSend))
        }
    }

    fun onEnter() = enqueue(TextCommand.Enter)

    fun showKeyboardSheet() {
        query.value = latestTvText() ?: ""
        setKeyboardSheetVisible(true, "user requested")
    }

    fun dismissKeyboardSheet() {
        setKeyboardSheetVisible(false, "user dismissed")
        query.value = ""
    }

    private fun setKeyboardSheetVisible(visible: Boolean, reason: String) {
        val previous = sheetVisible.value
        sheetVisible.value = visible
        if (previous != visible) {
            Log.i(
                TAG,
                "Phone keyboard sheet ${if (visible) "shown" else "hidden"}: $reason"
            )
        }
    }

    private companion object {
        const val TAG = "KeyboardViewModel"
    }
}

private fun String.toSingleLineText(): String =
    map { character ->
        if (
            character == '\r' ||
            character == '\n' ||
            character == '\u000B' ||
            character == '\u000C' ||
            character == '\u0085' ||
            character == '\u2028' ||
            character == '\u2029'
        ) {
            ' '
        } else {
            character
        }
    }.joinToString("")
