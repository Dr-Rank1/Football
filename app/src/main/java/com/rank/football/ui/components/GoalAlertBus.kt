package com.rank.football.ui.components

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class GoalAlert(
    val fixtureId: Int,
    val scorer: String,
    val team: String,
    val minute: Int,
    val score: String,
    val competition: String
)

/** App-wide goal toast events (sonner-style). */
object GoalAlertBus {
    private val _alerts = MutableSharedFlow<GoalAlert>(
        extraBufferCapacity = 8,
        replay = 0
    )
    val alerts: SharedFlow<GoalAlert> = _alerts.asSharedFlow()

    fun emit(alert: GoalAlert) {
        _alerts.tryEmit(alert)
    }
}
