package com.tuannguyen.remotelastv.domain.model


sealed interface PurchaseEvent {
    data object Success : PurchaseEvent
    data object Pending : PurchaseEvent
    data object Canceled : PurchaseEvent
    data class Failed(val responseCode: Int) : PurchaseEvent
}