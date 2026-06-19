package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.CineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CineRepository

    // Current navigation state
    private val _navScreen = MutableStateFlow("profiles") // "profiles", "home_screen", "player"
    val navScreen: StateFlow<String> = _navScreen.asStateFlow()

    // Profiles List (Observed from database)
    val profilesList: StateFlow<List<UserProfile>>

    // Selected user profile
    private val _activeProfile = MutableStateFlow<UserProfile?>(null)
    val activeProfile: StateFlow<UserProfile?> = _activeProfile.asStateFlow()

    // Media Details pop-up
    private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
    val selectedMedia: StateFlow<MediaItem?> = _selectedMedia.asStateFlow()

    // Currently playing media in player
    private val _playingMedia = MutableStateFlow<MediaItem?>(null)
    val playingMedia: StateFlow<MediaItem?> = _playingMedia.asStateFlow()

    // Search query & results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MediaItem>>(emptyList())
    val searchResults: StateFlow<List<MediaItem>> = _searchResults.asStateFlow()

    // Selected Player options (4K, Subtitles and Audio)
    private val _selectedAudio = MutableStateFlow("Português (Áudio)")
    val selectedAudio: StateFlow<String> = _selectedAudio.asStateFlow()

    private val _selectedSubtitle = MutableStateFlow("Português (PT)")
    val selectedSubtitle: StateFlow<String> = _selectedSubtitle.asStateFlow()

    private val _selectedQuality = MutableStateFlow("4K Ultra HD")
    val selectedQuality: StateFlow<String> = _selectedQuality.asStateFlow()

    // Active watchlists & histories
    private val _watchlist = MutableStateFlow<List<WatchListEntity>>(emptyList())
    val watchlist: StateFlow<List<WatchListEntity>> = _watchlist.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<WatchHistoryEntity>>(emptyList())
    val watchHistory: StateFlow<List<WatchHistoryEntity>> = _watchHistory.asStateFlow()

    // Online dynamic database via GitHub Integration
    private val apiService = com.example.data.api.CineApiService.create()
    
    private val _dbUrl = MutableStateFlow(com.example.data.api.CineApiService.DEFAULT_DATABASE_URL)
    val dbUrl: StateFlow<String> = _dbUrl.asStateFlow()

    private val _syncStatus = MutableStateFlow("Local (Offline)") // "Local (Offline)", "Sincronizado", "Erro ao Sincronizar", "Conectando..."
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CineRepository(database.cineDao())
        
        profilesList = repository.getAllProfiles()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Try downloading online dynamic database immediately on app launch
        refreshRemoteDatabase()

        // Listen for active profile changes to sync historical flows
        viewModelScope.launch {
            _activeProfile.collect { profile ->
                if (profile != null) {
                    launch {
                        repository.getWatchlistForProfile(profile.id).collect {
                            _watchlist.value = it
                        }
                    }
                    launch {
                        repository.getWatchHistoryForProfile(profile.id).collect {
                            _watchHistory.value = it
                        }
                    }
                } else {
                    _watchlist.value = emptyList()
                    _watchHistory.value = emptyList()
                }
            }
        }
    }

    // --- Profile Operations ---
    fun selectProfile(profile: UserProfile) {
        _activeProfile.value = profile
        _navScreen.value = "home_screen"
    }

    fun logoutProfile() {
        _activeProfile.value = null
        _navScreen.value = "profiles"
    }

    fun createProfile(name: String, avatarIndex: Int) {
        viewModelScope.launch {
            repository.insertProfile(name, avatarIndex)
        }
    }

    fun deleteProfile(profile: UserProfile) {
        viewModelScope.launch {
            if (_activeProfile.value?.id == profile.id) {
                _activeProfile.value = null
                _navScreen.value = "profiles"
            }
            repository.deleteProfile(profile)
        }
    }

    // --- Navigation ---
    fun setScreen(screen: String) {
        _navScreen.value = screen
    }

    // --- Media Details ---
    fun showMediaDetails(mediaItem: MediaItem) {
        _selectedMedia.value = mediaItem
    }

    fun removeMediaDetails() {
        _selectedMedia.value = null
    }

    // --- Watchlist Operations ---
    fun toggleWatchlist(mediaId: String) {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            val alreadyIn = _watchlist.value.any { it.mediaId == mediaId }
            if (alreadyIn) {
                repository.removeFromWatchlist(profileId, mediaId)
            } else {
                repository.addToWatchlist(profileId, mediaId)
            }
        }
    }

    fun isInWatchlist(mediaId: String): Boolean {
        return _watchlist.value.any { it.mediaId == mediaId }
    }

    // --- Watch History Operations ---
    fun saveWatchHistory(mediaId: String, progressSeconds: Long, durationSeconds: Long) {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            repository.saveWatchHistory(profileId, mediaId, progressSeconds, durationSeconds)
        }
    }

    fun clearWatchHistory() {
        val profileId = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            repository.clearHistory(profileId)
        }
    }

    // --- Search ---
    fun updateQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            _searchResults.value = repository.searchMedia(query)
        }
    }

    // --- Player Options ---
    fun startPlaying(mediaItem: MediaItem) {
        _playingMedia.value = mediaItem
        _selectedAudio.value = mediaItem.audioLanguages.firstOrNull() ?: "Português (Áudio)"
        _selectedSubtitle.value = mediaItem.subtitles.firstOrNull() ?: "Português (PT)"
        _selectedQuality.value = "4K Ultra HD"
        _navScreen.value = "player"
        _selectedMedia.value = null // Close popup details when starting playback
    }

    fun stopPlaying() {
        val playing = _playingMedia.value
        if (playing != null) {
            // Save mock current watch progress in seconds (e.g. 5200s out of 7200s)
            val durationMock = if (playing.type == MediaType.MOVIE) 7200L else 1500L
            val progressMock = (0.35 * durationMock).toLong() // 35% finished
            saveWatchHistory(playing.id, progressMock, durationMock)
        }
        _playingMedia.value = null
        _navScreen.value = "home_screen"
    }

    fun setAudioLanguage(language: String) {
        _selectedAudio.value = language
    }

    fun setSubtitle(subtitle: String) {
        _selectedSubtitle.value = subtitle
    }

    fun setVideoQuality(quality: String) {
        _selectedQuality.value = quality
    }

    // List of available mock items (for widgets)
    fun getMediaItems(): List<MediaItem> = repository.getMediaItems()
    fun getAnimes(): List<MediaItem> = repository.getAnimes()
    fun getMovies(): List<MediaItem> = repository.getMovies()
    fun getSeries(): List<MediaItem> = repository.getSeries()
    fun getTrending(): List<MediaItem> = repository.getTrending()
    fun getPopular(): List<MediaItem> = repository.getPopular()

    fun refreshRemoteDatabase(customUrl: String? = null) {
        val urlToUse = customUrl ?: _dbUrl.value
        _dbUrl.value = urlToUse
        _syncStatus.value = "Conectando..."
        
        viewModelScope.launch {
            try {
                // Call Retrofit service on dispatcher thread
                val remoteCatalog = apiService.getRemoteCatalog(urlToUse)
                if (remoteCatalog.isNotEmpty()) {
                    repository.updateMediaItems(remoteCatalog)
                    _syncStatus.value = "Sincronizado"
                } else {
                    _syncStatus.value = "Sem itens remotos"
                }
            } catch (e: Exception) {
                _syncStatus.value = "Erro"
                android.util.Log.e("CineViewModel", "Failed to deserialize or fetch dynamic list from: $urlToUse", e)
            }
        }
    }
}
