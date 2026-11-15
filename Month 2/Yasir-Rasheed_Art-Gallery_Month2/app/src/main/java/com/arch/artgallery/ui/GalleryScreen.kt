package com.arch.artgallery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arch.artgallery.data.Artwork
import com.arch.artgallery.data.Categories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    vm: GalleryViewModel,
    onOpen: (Long) -> Unit,
    onUpload: () -> Unit,
    onFavorites: () -> Unit
) {
    val art by vm.artworks.collectAsState()
    val query by vm.query.collectAsState()
    val category by vm.selectedCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Art Gallery", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onFavorites) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favorites")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onUpload) {
                Icon(Icons.Filled.Add, contentDescription = "Upload artwork")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                placeholder = { Text("Search title or artist") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems(Categories.all) { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { vm.selectedCategory.value = if (category == c) null else c },
                        label = { Text(c) }
                    )
                }
            }
            if (art.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No artwork found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                ArtGrid(art, onOpen, vm)
            }
        }
    }
}

@Composable
fun ArtGrid(items: List<Artwork>, onOpen: (Long) -> Unit, vm: GalleryViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { a -> ArtCard(a, onOpen, vm) }
    }
}

@Composable
private fun ArtCard(art: Artwork, onOpen: (Long) -> Unit, vm: GalleryViewModel) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.clickable { onOpen(art.id) }
    ) {
        Box {
            AsyncImage(
                model = art.imageUri,
                contentDescription = art.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(0.78f)
            )
            IconButton(
                onClick = { vm.toggleFavorite(art) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    if (art.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (art.isFavorite) MaterialTheme.colorScheme.secondary
                    else androidx.compose.ui.graphics.Color.White
                )
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(art.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                art.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 1
            )
        }
    }
}
