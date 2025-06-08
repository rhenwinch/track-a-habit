package io.track.habit.usecase

import io.track.habit.domain.repository.StreakRepository
import io.track.habit.domain.usecase.GetStreakUseCase
import io.track.habit.repository.fake.FakeStreakRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetStreakUseCaseTest {
    private lateinit var repository: StreakRepository
    private lateinit var getStreakUseCase: GetStreakUseCase

    @Before
    fun setUp() {
        repository = FakeStreakRepository()
        getStreakUseCase = GetStreakUseCase(repository)
    }

    @Test
    fun `getStreaksByDaysUseCase returns correct streak`() =
        runTest {
            val days = 10
            val streak = getStreakUseCase(days)
            assert(days >= streak.minDaysRequired && days <= streak.maxDaysRequired)
        }
}
