package com.tuannguyen.remotelastv.ui.home

data class HomeUiState(
    val connectedDeviceName: String = "SWTV-22AE",
    val isConnected: Boolean = false,
    val selectedPage: Int = 0,
    val volume: Int = 20,
    val isMuted: Boolean = false,
    val channel: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
)

sealed interface HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect
}
