package com.rank.football.performance

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rank.football.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Room maintenance helpers for indexes, vacuum, and stale row pruning. */
object DatabaseOptimizer {

    /** Runs ANALYZE and prunes stale cache tables on a background dispatcher. */
    suspend fun optimize(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        db.articleCacheDao().purgeBefore(cutoff)
        db.openHelper.writableDatabase.runMaintenance()
    }

    /** Executes SQLite maintenance pragmas on the open database. */
    private fun SupportSQLiteDatabase.runMaintenance() {
        execSQL("ANALYZE")
        execSQL("PRAGMA optimize")
    }
}
