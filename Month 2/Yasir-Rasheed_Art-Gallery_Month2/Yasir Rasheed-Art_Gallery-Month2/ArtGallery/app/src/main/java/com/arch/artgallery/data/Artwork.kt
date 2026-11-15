package com.arch.artgallery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artworks")
data class Artwork(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val description: String,
    val category: String,
    val imageUri: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object Categories {
    val all = listOf("Abstract", "Portrait", "Landscape", "Surreal", "Digital Painting", "Illustration")
}
