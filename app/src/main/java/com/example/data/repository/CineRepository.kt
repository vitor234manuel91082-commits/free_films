package com.example.data.repository

import com.example.data.database.CineDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CineRepository(private val cineDao: CineDao) {

    // --- Database Queries (Profiles, Watchlist & History) ---
    
    fun getAllProfiles(): Flow<List<UserProfile>> = cineDao.getAllProfiles()

    suspend fun insertProfile(name: String, avatarIndex: Int): Long {
        val profile = UserProfile(name = name, avatarIndex = avatarIndex)
        return cineDao.insertProfile(profile)
    }

    suspend fun deleteProfile(profile: UserProfile) {
        cineDao.deleteProfile(profile)
    }

    fun getWatchlistForProfile(profileId: Int): Flow<List<WatchListEntity>> = 
        cineDao.getWatchlistForProfile(profileId)

    suspend fun addToWatchlist(profileId: Int, mediaId: String) {
        cineDao.addToWatchlist(WatchListEntity(profileId = profileId, mediaId = mediaId))
    }

    suspend fun removeFromWatchlist(profileId: Int, mediaId: String) {
        cineDao.removeFromWatchlist(profileId, mediaId)
    }

    fun isInWatchlist(profileId: Int, mediaId: String): Flow<Boolean> = 
        cineDao.isInWatchlist(profileId, mediaId)

    fun getWatchHistoryForProfile(profileId: Int): Flow<List<WatchHistoryEntity>> = 
        cineDao.getWatchHistoryForProfile(profileId)

    suspend fun saveWatchHistory(profileId: Int, mediaId: String, progressSeconds: Long, durationSeconds: Long) {
        val history = WatchHistoryEntity(
            profileId = profileId,
            mediaId = mediaId,
            progressSeconds = progressSeconds,
            durationSeconds = durationSeconds,
            watchedAt = System.currentTimeMillis()
        )
        cineDao.saveWatchHistory(history)
    }

    suspend fun removeFromHistory(profileId: Int, mediaId: String) {
        cineDao.removeFromHistory(profileId, mediaId)
    }

    suspend fun clearHistory(profileId: Int) {
        cineDao.clearHistory(profileId)
    }

    // --- Native Media Items (In-memory 4K Catalog) ---

    private var cachedMediaItems: List<MediaItem> = getMockMediaItems()

    fun getMediaItems(): List<MediaItem> = cachedMediaItems

    fun updateMediaItems(items: List<MediaItem>) {
        if (items.isNotEmpty()) {
            cachedMediaItems = items
        }
    }

    fun getMediaItem(id: String): MediaItem? {
        return cachedMediaItems.firstOrNull { it.id == id }
    }

    fun getAnimes(): List<MediaItem> {
        return cachedMediaItems.filter { it.type == MediaType.ANIME }
    }

    fun getMovies(): List<MediaItem> {
        return cachedMediaItems.filter { it.type == MediaType.MOVIE }
    }

    fun getSeries(): List<MediaItem> {
        return cachedMediaItems.filter { it.type == MediaType.SERIES }
    }

    fun getTrending(): List<MediaItem> {
        return cachedMediaItems.filter { it.trending }
    }

    fun getPopular(): List<MediaItem> {
        return cachedMediaItems.filter { it.popular }
    }

    fun searchMedia(query: String): List<MediaItem> {
        if (query.isBlank()) return emptyList()
        return cachedMediaItems.filter {
            it.title.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true) ||
            it.genres.any { genre -> genre.contains(query, ignoreCase = true) }
        }
    }
}
