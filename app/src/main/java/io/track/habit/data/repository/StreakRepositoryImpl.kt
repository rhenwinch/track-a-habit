package io.track.habit.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.repository.StreakRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

const val STREAKS_FILE_NAME = "streaks.json"

class StreakRepositoryImpl
    @Inject
    constructor(
        private val assetReader: AssetReader,
        private val gson: Gson = Gson(),
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
    ) : StreakRepository {
        private var streaks: List<Streak>? = null

        init {
            CoroutineScope(dispatcher + Job()).launch {
                streaks = loadStreaksFromAssets()
            }
        }

        override fun getAllStreaks(): List<Streak> {
            return streaks ?: runBlocking {
                loadStreaksFromAssets().also {
                    streaks = it
                }
            }
        }

        private suspend fun loadStreaksFromAssets(): List<Streak> {
            val json = assetReader.read(STREAKS_FILE_NAME)
            val type = object : TypeToken<List<Streak>>() {}.type
            return gson.fromJson(json, type)
        }
    }
