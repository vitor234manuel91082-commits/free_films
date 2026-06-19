package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val avatarIndex: Int = 0, // 0 to 4 for different colored Netflix icon styles
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val mediaId: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressSeconds: Long,
    val durationSeconds: Long
)

@Entity(tableName = "watchlist")
data class WatchListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val mediaId: String,
    val addedAt: Long = System.currentTimeMillis()
)
