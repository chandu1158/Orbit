package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val db: AppDatabase) {
    val favorites: Flow<List<FavoriteEntity>> = db.favoriteDao().getAllFavorites()

    suspend fun addFavorite(favorite: FavoriteEntity): Long {
        return db.favoriteDao().insert(favorite)
    }

    suspend fun removeFavorite(favorite: FavoriteEntity) {
        db.favoriteDao().delete(favorite)
    }

    suspend fun removeByContactId(contactId: Long) {
        db.favoriteDao().deleteByContactId(contactId)
    }

    suspend fun removeByPhoneNumber(phoneNumber: String) {
        db.favoriteDao().deleteByPhoneNumber(phoneNumber)
    }

    suspend fun isFavorite(phoneNumber: String): Boolean {
        return db.favoriteDao().getByPhoneNumber(phoneNumber) != null
    }

    suspend fun updateOrder(favorites: List<FavoriteEntity>) {
        db.favoriteDao().update(favorites)
    }
}
