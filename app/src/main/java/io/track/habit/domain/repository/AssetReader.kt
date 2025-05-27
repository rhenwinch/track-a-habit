package io.track.habit.domain.repository

interface AssetReader {
    suspend fun read(fileName: String): String
}
