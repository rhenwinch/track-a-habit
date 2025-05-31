package io.track.habit.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.repository.StreakRepository
import javax.inject.Inject

const val STREAKS_FILE_NAME = "streaks.json"

class StreakRepositoryImpl
    @Inject
    constructor(
        private val assetReader: AssetReader,
        private val gson: Gson = Gson(),
    ) : StreakRepository {
        private var cachedStreaks: List<Streak>? = null

        override suspend fun getAllStreaks(): List<Streak> {
            return cachedStreaks ?: loadStreaksFromAssets().also {
                cachedStreaks = it
            }
        }

        private suspend fun loadStreaksFromAssets(): List<Streak> {
            val json = assetReader.read(STREAKS_FILE_NAME)
            val type = object : TypeToken<List<Streak>>() {}.type
            return gson.fromJson(json, type)
        }
    }
