package io.track.habit.usecase

import io.mockk.coEvery
import io.mockk.mockk
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.usecase.GetRandomQuoteUseCase
import io.track.habit.domain.usecase.QUOTES_FILE_NAME
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetRandomQuoteUseCaseTest {
    private lateinit var assetReader: AssetReader
    private lateinit var getRandomQuoteUseCase: GetRandomQuoteUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher + Job())

    @Before
    fun setUp() {
        assetReader = mockk()
        coEvery { assetReader.read(QUOTES_FILE_NAME) } returns
            """
            [
                {
                    "message": "Test Quote",
                    "author": "Test Author"
                }
            ]
            """.trimIndent()

        getRandomQuoteUseCase = GetRandomQuoteUseCase(assetReader, dispatcher)
    }

    @Test
    fun `getRandomQuoteUseCase returns a random quote`() =
        scope.runTest {
            val quote = getRandomQuoteUseCase()
            assert(quote.message == "Test Quote")
            assert(quote.author == "Test Author")
        }
}
