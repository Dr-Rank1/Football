package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_events")
data class AnalyticsEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventName: String,
    val params: String,
    val timestamp: Long = System.currentTimeMillis()
)
