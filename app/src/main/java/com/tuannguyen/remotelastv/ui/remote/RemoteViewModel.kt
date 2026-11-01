package com.tuannguyen.remotelastv.ui.remote

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.manager.TvControllerManager
import com.tuannguyen.remotelastv.domain.manager.TvTextInputManager
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.model.VolumeState
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val controllerManager: TvControllerManager,
    private val textInputManager: TvTextInputManager,
) : ViewModel() {

    private sealed interface TextCommand {
        data class Replace(val text: String) : TextCommand
        data class Send(val text: String) : TextCommand
        data class Delete(val count: Int) : TextCommand
        data object Enter : TextCommand
    }

    private data class RemoteLiveState(
        val touchpadMode: TouchpadMode,
        val volume: VolumeState?,
        val capabilities: Set<TvCapability>,
        val isRecording: Boolean,
    )

    private val remoteLiveState: Flow<RemoteLiveState> = combine(
        controllerManager.touchpadMode,
        controllerManager.volume,
        controllerManager.capabilities,
        controllerManager.voiceRecording,
    ) { touchpadMode, volume, capabilities, isRecording ->
        RemoteLiveState(touchpadMode, volume, capabilities, isRecording)
    }

    private val _localState = MutableStateFlow(RemoteUiState())

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

    private val keyboardUiState: Flow<KeyboardUiState> = combine(
        sheetVisible,
        query,
        tvInput,
        textInputManager.capabilities,
    ) { visible, currentQuery, input, capabilities ->
        KeyboardUiState(
            query = currentQuery,
            input = input,
            inputHint = (input as? TextInputState.Focused)?.label,
            canObserveFocus = TextCapability.OBSERVE_FOCUS in capabilities,
            showKeyboardSheet = visible,
        )
    }

    private val isAutoShowKeyboardEnabled: StateFlow<Boolean> =
        settingsRepository.remoteSettings
            .map { it.isAutoShowKeyboardEnabled }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _effect = Channel<RemoteEvent>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<RemoteUiState> = combine(
        _localState,
        settingsRepository.remoteSettings,
        remoteLiveState,
        keyboardUiState,
    ) { local, settings, live, keyboard ->
        local.copy(
            showMediaButtons = settings.showMediaButtons,
            navigationMode = settings.navigationMode,
            isAutoReconnectEnabled = settings.isAutoReconnectEnabled,
            isKeepScreenAwakeEnabled = settings.isKeepScreenAwakeEnabled,
            isTheaterModeEnabled = settings.isTheaterModeEnabled,
            isHapticFeedbackEnabled = settings.isHapticFeedbackEnabled,
            isSmoothPressEnabled = settings.isSmoothPressEnabled,
            isAutoShowKeyboardEnabled = settings.isAutoShowKeyboardEnabled,
            hasSeenRemoteTutorial = settings.hasSeenRemoteTutorial,
            hasRequestedMicrophonePermission = settings.hasRequestedMicrophonePermission,
            touchpadMode = live.touchpadMode,
            volume = live.volume,
            capabilities = live.capabilities,
            isRecording = live.isRecording,
            keyboard = keyboard,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemoteUiState(),
    )

    private var pendingDx = 0f
    private var pendingDy = 0f
    private var flushJob: Job? = null

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
                                "TV text field ready for input (label=${label.orEmpty()})",
                            )
                        } else {
                            Log.d(
                                TAG,
                                "TV text field focused; phone keyboard sheet not auto-shown " +
                                    "(label=${label.orEmpty()}, autoShow=$autoShowOnFocus, " +
                                    "setting=${isAutoShowKeyboardEnabled.value})",
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
                        "TV sent keyboard show request; ignored because auto-show is disabled",
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
            for (command in commands) {
                try {
                    process(command)
                } finally {
                    pending.decrementAndGet()
                }
            }
        }
    }

    fun onRemoteButtonClick(key: TvKey) = send { it.sendKey(key) }

    fun startVoiceRecording() {
        viewModelScope.launch {
            controllerManager.execute { it.startVoiceRecording() }
                .onFailure { error ->
                    _effect.send(
                        RemoteEvent.ShowMessage(
                            "Unable to start voice recording: ${error.message}",
                        ),
                    )
                }
        }
    }

    fun stopVoiceRecording() {
        viewModelScope.launch {
            controllerManager.execute { it.stopVoiceRecording() }
                .onFailure { error ->
                    _effect.send(
                        RemoteEvent.ShowMessage(
                            "Unable to stop voice recording: ${error.message}",
                        ),
                    )
                }
        }
    }

    fun onMicrophonePermissionDenied() {
        viewModelScope.launch {
            _effect.send(
                RemoteEvent.ShowMessage(
                    "Microphone permission denied. Enable it in app settings to use voice.",
                ),
            )
        }
    }

    fun onMicrophonePermissionRequested() {
        viewModelScope.launch {
            settingsRepository.setHasRequestedMicrophonePermission(true)
        }
    }

    fun onPageSelected(page: Int) {
        _localState.value = _localState.value.copy(selectedPage = page)
    }

    fun onTouchpad(event: TouchpadEvent) {
        if (event !is TouchpadEvent.Move) {
            send { it.sendTouchpad(event) }
            return
        }
        pendingDx += event.dx
        pendingDy += event.dy
        if (flushJob?.isActive == true) return
        flushJob = viewModelScope.launch {
            delay(10)
            val dx = pendingDx
            val dy = pendingDy
            pendingDx = 0f
            pendingDy = 0f
            send { it.sendTouchpad(TouchpadEvent.Move(dx, dy)) }
        }
    }

    fun onTutorialFinished() = updateSettings {
        setHasSeenRemoteTutorial(true)
    }

    fun onTutorialNavigationModeChanged(mode: NavigationMode) = updateSettings {
        setNavigationMode(mode)
    }

    fun onTutorialMediaControlsChanged(enabled: Boolean) = updateSettings {
        setShowMediaButtons(enabled)
    }

    fun onTutorialAutoReconnectChanged(enabled: Boolean) = updateSettings {
        setAutoReconnectEnabled(enabled)
    }

    fun onTutorialKeepScreenAwakeChanged(enabled: Boolean) = updateSettings {
        setKeepScreenAwakeEnabled(enabled)
    }

    fun onTutorialTheaterModeChanged(enabled: Boolean) = updateSettings {
        setTheaterModeEnabled(enabled)
    }

    fun onTutorialHapticFeedbackChanged(enabled: Boolean) = updateSettings {
        setHapticFeedbackEnabled(enabled)
    }

    fun onTutorialSmoothPressChanged(enabled: Boolean) = updateSettings {
        setSmoothPressEnabled(enabled)
    }

    fun onTutorialAutoShowKeyboardChanged(enabled: Boolean) = updateSettings {
        setAutoShowKeyboardEnabled(enabled)
    }

    fun onQueryChange(newValue: String) {
        val normalized = newValue.toSingleLineText()
        val old = query.value
        if (normalized == old) return
        query.value = normalized

        if (TextCapability.REPLACE_TEXT in capsSnapshot.value) {
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

    private suspend fun process(command: TextCommand) {
        when (command) {
            is TextCommand.Replace -> textInputManager.execute { it.replaceText(command.text) }
            is TextCommand.Send -> textInputManager.execute { it.sendText(command.text) }
            is TextCommand.Delete -> repeat(command.count) {
                textInputManager.execute { it.sendDelete() }
            }
            TextCommand.Enter -> textInputManager.execute { it.sendEnter() }
        }
    }

    private fun enqueue(command: TextCommand) {
        pending.incrementAndGet()
        if (!commands.trySend(command).isSuccess) pending.decrementAndGet()
    }

    private fun setKeyboardSheetVisible(visible: Boolean, reason: String) {
        val previous = sheetVisible.value
        sheetVisible.value = visible
        if (previous != visible) {
            Log.i(
                TAG,
                "Phone keyboard sheet ${if (visible) "shown" else "hidden"}: $reason",
            )
        }
    }

    private fun send(block: suspend (TvControllerRepository) -> Result<Unit>) {
        viewModelScope.launch {
            controllerManager.execute(block).onFailure { error ->
                if (error is TvException.NotConnected) {
                    _effect.send(RemoteEvent.NotConnected)
                } else {
                    _effect.send(RemoteEvent.ShowMessage("Remote action failed: ${error.message}"))
                }
            }
        }
    }

    private fun updateSettings(update: suspend SettingsRepository.() -> Unit) {
        viewModelScope.launch { update(settingsRepository) }
    }

    private companion object {
        const val TAG = "RemoteViewModel"
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
