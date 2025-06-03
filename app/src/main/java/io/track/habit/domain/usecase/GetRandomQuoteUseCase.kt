package io.track.habit.domain.usecase

import com.google.gson.Gson
import io.track.habit.domain.model.Quote
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.utils.coroutines.AppDispatcher.Companion.launchOnIO
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

const val QUOTES_FILE_NAME = "quotes.json"

@Singleton
class GetRandomQuoteUseCase
    @Inject
    constructor(
        private val assetReader: AssetReader,
    ) {
        private val gson: Gson by lazy { Gson() }
        private lateinit var quotes: List<Quote>

        init {
            launchOnIO {
                val quotesAsset = assetReader.read(QUOTES_FILE_NAME)
                quotes = gson.fromJson(quotesAsset, Array<Quote>::class.java).toList()
            }
        }

        operator fun invoke(): Quote {
            if (::quotes.isInitialized) return quotes.random()

            val quotesAsset = runBlocking { assetReader.read(QUOTES_FILE_NAME) }
            quotes = gson.fromJson(quotesAsset, Array<Quote>::class.java).toList()

            return quotes.random()
        }
    }
