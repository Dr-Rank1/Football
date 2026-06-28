package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ad_impressions")
data class AdImpressionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val adType: String,
    val day: String,
    val count: Int = 1
)
