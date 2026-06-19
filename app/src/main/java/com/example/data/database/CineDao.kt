package com.example.data.database

import androidx.room.*
import com.example.data.model.UserProfile
import com.example.data.model.WatchHistoryEntity
import com.example.data.model.WatchListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CineDao {

    // Profiles
    @Query("SELECT * FROM user_profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    // Watchlist
    @Query("SELECT * FROM watchlist WHERE profileId = :profileId ORDER BY addedAt DESC")
    fun getWatchlistForProfile(profileId: Int): Flow<List<WatchListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(watchList: WatchListEntity)

    @Query("DELETE FROM watchlist WHERE profileId = :profileId AND mediaId = :mediaId")
    suspend fun removeFromWatchlist(profileId: Int, mediaId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE profileId = :profileId AND mediaId = :mediaId LIMIT 1)")
    fun isInWatchlist(profileId: Int, mediaId: String): Flow<Boolean>

    // Watch History
    @Query("SELECT * FROM watch_history WHERE profileId = :profileId ORDER BY watchedAt DESC")
    fun getWatchHistoryForProfile(profileId: Int): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId AND mediaId = :mediaId")
    suspend fun removeFromHistory(profileId: Int, mediaId: String)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId")
    suspend fun clearHistory(profileId: Int)
}
