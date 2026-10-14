package com.tuannguyen.remotelastv.domain.connection

import kotlinx.coroutines.flow.StateFlow

interface AppLifecycleTracker {
    val isForeground: StateFlow<Boolean>
}
