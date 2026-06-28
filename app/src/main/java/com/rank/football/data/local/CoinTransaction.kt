package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin_ledger")
data class CoinTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
