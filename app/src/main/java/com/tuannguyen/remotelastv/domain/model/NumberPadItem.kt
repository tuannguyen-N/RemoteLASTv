package com.tuannguyen.remotelastv.domain.model

sealed interface NumberPadItem {
    data class Number(val value: Int) : NumberPadItem
    data object Empty : NumberPadItem
    data object Delete : NumberPadItem
}