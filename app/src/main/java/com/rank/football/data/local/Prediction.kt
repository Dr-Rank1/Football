package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey val fixtureId: Int,
    val predictedWinner: String,
    val predictedScore: String,
    val pointsEarned: Int = 0,
    val resolved: Boolean = false
)
