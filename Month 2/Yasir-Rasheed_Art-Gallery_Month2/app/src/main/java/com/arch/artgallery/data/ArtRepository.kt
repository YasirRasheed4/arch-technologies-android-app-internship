package com.arch.artgallery.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ArtRepository(private val context: Context, private val dao: ArtworkDao) {

    val artworks: Flow<List<Artwork>> = dao.observeAll()

    fun artwork(id: Long): Flow<Artwork?> = dao.observeById(id)

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) dao.insertAll(SampleData.items)
    }

    /** Copies the picked image into app storage so access survives restarts. */
    suspend fun addArtwork(
        title: String,
        artist: String,
        description: String,
        category: String,
        source: Uri
    ) = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "art").apply { mkdirs() }
        val target = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            target.outputStream().use { out -> input.copyTo(out) }
        }
        dao.insert(
            Artwork(
                title = title.trim(),
                artist = artist.trim(),
                description = description.trim(),
                category = category,
                imageUri = Uri.fromFile(target).toString()
            )
        )
    }

    suspend fun toggleFavorite(art: Artwork) = dao.setFavorite(art.id, !art.isFavorite)

    suspend fun delete(art: Artwork) = withContext(Dispatchers.IO) {
        if (art.imageUri.startsWith("file:")) {
            runCatching { File(Uri.parse(art.imageUri).path!!).delete() }
        }
        dao.delete(art)
    }
}

object SampleData {
    private fun img(seed: String) = "https://picsum.photos/seed/$seed/700/900"

    val items = listOf(
        Artwork(title = "Neon Dusk", artist = "Ayesha Khan", category = "Digital Painting",
            description = "A city skyline melting into a violet evening sky.", imageUri = img("neon-dusk")),
        Artwork(title = "Quiet Valley", artist = "Hamza Ali", category = "Landscape",
            description = "Morning mist rolling over a silent green valley.", imageUri = img("quiet-valley")),
        Artwork(title = "Fragments", artist = "Sara Malik", category = "Abstract",
            description = "Overlapping geometry exploring memory and noise.", imageUri = img("fragments")),
        Artwork(title = "The Watcher", artist = "Bilal Ahmed", category = "Surreal",
            description = "An oversized eye floating above a calm desert.", imageUri = img("the-watcher")),
        Artwork(title = "Portrait in Teal", artist = "Maryam Noor", category = "Portrait",
            description = "A study of light and skin tone in cool colours.", imageUri = img("portrait-teal")),
        Artwork(title = "Paper Birds", artist = "Usman Tariq", category = "Illustration",
            description = "Folded paper birds drifting through a sunrise.", imageUri = img("paper-birds")),
        Artwork(title = "Ocean Static", artist = "Ayesha Khan", category = "Abstract",
            description = "Texture and colour inspired by waves on a screen.", imageUri = img("ocean-static")),
        Artwork(title = "Last Train", artist = "Hamza Ali", category = "Digital Painting",
            description = "A lone platform lit by the final evening train.", imageUri = img("last-train"))
    )
}
