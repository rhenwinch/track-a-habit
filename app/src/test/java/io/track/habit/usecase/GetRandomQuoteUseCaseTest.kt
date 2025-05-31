package io.track.habit.usecase

import io.mockk.coEvery
import io.mockk.mockk
import io.track.habit.domain.model.Quote
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.usecase.GetRandomQuoteUseCase
import io.track.habit.domain.usecase.QUOTES_FILE_NAME
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetRandomQuoteUseCaseTest {
    private lateinit var assetReader: AssetReader
    private lateinit var getRandomQuoteUseCase: GetRandomQuoteUseCase

    @Before
    fun setUp() {
        assetReader = mockk()
        getRandomQuoteUseCase = GetRandomQuoteUseCase(assetReader)

        coEvery { assetReader.read(QUOTES_FILE_NAME) } returns ""
        coEvery { getRandomQuoteUseCase.invoke() } returns
            Quote(
                message = "Test Quote",
                author = "Test Author",
            )
    }

    @Test
    fun `getRandomQuoteUseCase returns a random quote`() =
        runBlocking {
            val quote = getRandomQuoteUseCase()
            assert(quote.message == "Test Quote")
            assert(quote.author == "Test Author")
        }
}
