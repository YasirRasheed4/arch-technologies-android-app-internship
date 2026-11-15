package com.arch.artgallery.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arch.artgallery.data.AppDatabase
import com.arch.artgallery.data.ArtRepository
import com.arch.artgallery.data.Artwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalleryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ArtRepository(app, AppDatabase.get(app).artworkDao())

    val query = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)

    val artworks: StateFlow<List<Artwork>> = combine(
        repo.artworks, query, selectedCategory
    ) { list, q, cat ->
        list.filter { art ->
            (cat == null || art.category == cat) &&
                (q.isBlank() ||
                    art.title.contains(q, ignoreCase = true) ||
                    art.artist.contains(q, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<Artwork>> = repo.artworks
        .combine(MutableStateFlow(Unit)) { list, _ -> list.filter { it.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repo.seedIfEmpty() }
    }

    fun artwork(id: Long): Flow<Artwork?> = repo.artwork(id)

    fun toggleFavorite(art: Artwork) = viewModelScope.launch { repo.toggleFavorite(art) }

    fun delete(art: Artwork) = viewModelScope.launch { repo.delete(art) }

    fun upload(
        title: String,
        artist: String,
        description: String,
        category: String,
        uri: Uri,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repo.addArtwork(title, artist, description, category, uri)
            onDone()
        }
    }
}
