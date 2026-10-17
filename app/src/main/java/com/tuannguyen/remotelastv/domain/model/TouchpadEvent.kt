package com.tuannguyen.remotelastv.domain.model

sealed interface TouchpadEvent {
    data class Move(val dx: Float, val dy: Float) : TouchpadEvent
    data class Scroll(val dx: Float, val dy: Float) : TouchpadEvent
    data object Tap : TouchpadEvent
}