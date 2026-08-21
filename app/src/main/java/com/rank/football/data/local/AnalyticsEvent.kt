package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analytics_events",
    indices = [Index("timestamp"), Index("eventName")]
)
data class AnalyticsEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventName: String,
    val params: String,
    val timestamp: Long = System.currentTimeMillis()
)
