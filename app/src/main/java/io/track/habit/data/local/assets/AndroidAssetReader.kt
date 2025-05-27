package io.track.habit.data.local.assets

import android.content.Context
import io.track.habit.domain.repository.AssetReader
import io.track.habit.utils.coroutines.AppDispatcher.Companion.withIOContext
import javax.inject.Inject

class AndroidAssetReader
    @Inject
    constructor(
        private val context: Context,
    ) : AssetReader {
        override suspend fun read(fileName: String): String =
            withIOContext {
                context.assets
                    .open(fileName)
                    .bufferedReader()
                    .use { it.readText() }
            }
    }
