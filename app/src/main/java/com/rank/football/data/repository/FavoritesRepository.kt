package com.rank.football.data.repository

import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.FavoriteTeam
import com.rank.football.data.local.FavoritesDao
import kotlinx.coroutines.flow.Flow

class FavoritesRepository private constructor(
    private val dao: FavoritesDao
) {

    constructor(database: AppDatabase) : this(database.favoritesDao())

    val allFavorites: Flow<List<FavoriteTeam>> = dao.getAllFavorites()

    fun isFavorite(teamId: Int): Flow<Boolean> = dao.isFavorite(teamId)

    suspend fun toggleFavorite(team: FavoriteTeam, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.deleteFavorite(team.teamId)
        } else {
            dao.insertFavorite(team)
        }
    }

    suspend fun addFavorite(team: FavoriteTeam) = dao.insertFavorite(team)
}
