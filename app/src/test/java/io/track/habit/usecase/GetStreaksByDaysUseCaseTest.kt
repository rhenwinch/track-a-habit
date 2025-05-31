package io.track.habit.usecase

import io.track.habit.domain.repository.StreakRepository
import io.track.habit.domain.usecase.GetStreaksByDaysUseCase
import io.track.habit.repository.fake.FakeStreakRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetStreaksByDaysUseCaseTest {
    private lateinit var repository: StreakRepository
    private lateinit var getStreaksByDaysUseCase: GetStreaksByDaysUseCase

    @Before
    fun setUp() {
        repository = FakeStreakRepository()
        getStreaksByDaysUseCase = GetStreaksByDaysUseCase(repository)
    }

    @Test
    fun `getStreaksByDaysUseCase returns correct streak`() =
        runTest {
            val days = 10
            val streak = getStreaksByDaysUseCase(days)
            assert(days >= streak.minDaysRequired && days <= streak.maxDaysRequired)
        }
}
