package com.arch.artgallery.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Artwork>>

    @Query("SELECT * FROM artworks WHERE id = :id")
    fun observeById(id: Long): Flow<Artwork?>

    @Query("SELECT COUNT(*) FROM artworks")
    suspend fun count(): Int

    @Insert
    suspend fun insert(artwork: Artwork): Long

    @Insert
    suspend fun insertAll(items: List<Artwork>)

    @Query("UPDATE artworks SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Delete
    suspend fun delete(artwork: Artwork)
}
