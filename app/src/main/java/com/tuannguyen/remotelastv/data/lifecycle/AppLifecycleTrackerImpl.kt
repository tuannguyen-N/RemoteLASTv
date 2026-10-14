package com.tuannguyen.remotelastv.data.lifecycle

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.tuannguyen.remotelastv.domain.connection.AppLifecycleTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AppLifecycleTrackerImpl @Inject constructor(
    @ApplicationContext context: Context
) : AppLifecycleTracker, Application.ActivityLifecycleCallbacks {

    private val _isForeground = MutableStateFlow(true)
    override val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    private var startedActivities = 0

    init {
        val app = context.applicationContext as Application
        app.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        startedActivities++
        if (startedActivities == 1) {
            _isForeground.value = true
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities--
        if (startedActivities <= 0) {
            startedActivities = 0
            _isForeground.value = false
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
