package io.track.habit.repository

import io.track.habit.domain.repository.StreakRepository
import io.track.habit.repository.fake.FakeStreakRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@Suppress("ktlint:standard:function-naming")
class StreakRepositoryTest {
    private lateinit var repository: StreakRepository

    @Before
    fun setUp() {
        repository = FakeStreakRepository()
    }

    @Test
    fun `getStreakByDaysRequired returns correct streak`() =
        runTest {
            val days = 10
            val streak = repository.getStreakByDaysRequired(days)
            assert(days >= streak.minDaysRequired && days <= streak.maxDaysRequired)
        }
}
