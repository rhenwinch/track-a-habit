package io.track.habit.data.local.assets

import android.content.Context
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.repository.AssetReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AndroidAssetReader
    @Inject
    constructor(
        private val context: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AssetReader {
        override suspend fun read(fileName: String): String =
            withContext(ioDispatcher) {
                context.assets
                    .open(fileName)
                    .bufferedReader()
                    .use { it.readText() }
            }
    }
