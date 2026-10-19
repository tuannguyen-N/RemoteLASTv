package com.tuannguyen.remotelastv.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object DeviceDiscovery : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object RemoteLayout : Route

    @Serializable
    data object RemoteBehavior : Route

    @Serializable
    data object ConnectionDisplay : Route

    @Serializable
    data object ConnectedDevices : Route

    @Serializable
    data object Faq : Route

    @Serializable
    data object Legal : Route
}