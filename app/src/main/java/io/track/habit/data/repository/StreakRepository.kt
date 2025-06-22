package io.track.habit.data.repository

import io.track.habit.R
import io.track.habit.domain.model.Streak
import io.track.habit.domain.utils.drawableRes
import io.track.habit.domain.utils.stringRes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepository
    @Inject
    constructor() {
        fun getAllStreaks(): List<Streak> {
            return listOf(
                Streak(
                    title = stringRes(R.string.streak_item_first_step_title),
                    minDaysRequired = 0,
                    maxDaysRequired = 6,
                    badgeIcon = drawableRes(R.drawable.badge_first_step),
                    message = stringRes(R.string.streak_item_first_step_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_week_warrior_title),
                    minDaysRequired = 7,
                    maxDaysRequired = 13,
                    badgeIcon = drawableRes(R.drawable.badge_week_warrior),
                    message = stringRes(R.string.streak_item_week_warrior_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_fortnight_fighter_title),
                    minDaysRequired = 14,
                    maxDaysRequired = 20,
                    badgeIcon = drawableRes(R.drawable.badge_shield),
                    message = stringRes(R.string.streak_item_fortnight_fighter_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_21_savage_title),
                    minDaysRequired = 21,
                    maxDaysRequired = 29,
                    badgeIcon = drawableRes(R.drawable.badge_rapper),
                    message = stringRes(R.string.streak_item_21_savage_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_monthly_master_title),
                    minDaysRequired = 30,
                    maxDaysRequired = 59,
                    badgeIcon = drawableRes(R.drawable.badge_calendar),
                    message = stringRes(R.string.streak_item_monthly_master_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_habit_hacker_title),
                    minDaysRequired = 60,
                    maxDaysRequired = 89,
                    badgeIcon = drawableRes(R.drawable.badge_keyboard),
                    message = stringRes(R.string.streak_item_habit_hacker_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_quarterly_champion_title),
                    minDaysRequired = 90,
                    maxDaysRequired = 99,
                    badgeIcon = drawableRes(R.drawable.badge_trophy),
                    message = stringRes(R.string.streak_item_quarterly_champion_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_centennial_gemstone_title),
                    minDaysRequired = 100,
                    maxDaysRequired = 149,
                    badgeIcon = drawableRes(R.drawable.badge_gemstone),
                    message = stringRes(R.string.streak_item_centennial_gemstone_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_zen_achiever_title),
                    minDaysRequired = 150,
                    maxDaysRequired = 179,
                    badgeIcon = drawableRes(R.drawable.badge_lotus_flower),
                    message = stringRes(R.string.streak_item_zen_achiever_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_half_year_hero_title),
                    minDaysRequired = 180,
                    maxDaysRequired = 269,
                    badgeIcon = drawableRes(R.drawable.badge_hero),
                    message = stringRes(R.string.streak_item_half_year_hero_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_endurance_champion_title),
                    minDaysRequired = 270,
                    maxDaysRequired = 364,
                    badgeIcon = drawableRes(R.drawable.badge_stopwatch),
                    message = stringRes(R.string.streak_item_endurance_champion_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_one_year_legend_title),
                    minDaysRequired = 365,
                    maxDaysRequired = 399,
                    badgeIcon = drawableRes(R.drawable.badge_crown),
                    message = stringRes(R.string.streak_item_one_year_legend_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_nietzsche_ubermensch_title),
                    minDaysRequired = 400,
                    maxDaysRequired = 454,
                    badgeIcon = drawableRes(R.drawable.badge_mustache),
                    message = stringRes(R.string.streak_item_nietzsche_ubermensch_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_socrates_apprentice_title),
                    minDaysRequired = 455,
                    maxDaysRequired = 544,
                    badgeIcon = drawableRes(R.drawable.badge_scroll),
                    message = stringRes(R.string.streak_item_socrates_apprentice_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_plato_pupil_title),
                    minDaysRequired = 545,
                    maxDaysRequired = 634,
                    badgeIcon = drawableRes(R.drawable.badge_book),
                    message = stringRes(R.string.streak_item_plato_pupil_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_aristotle_achiever_title),
                    minDaysRequired = 635,
                    maxDaysRequired = 729,
                    badgeIcon = drawableRes(R.drawable.badge_quill),
                    message = stringRes(R.string.streak_item_aristotle_achiever_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_two_year_titan_title),
                    minDaysRequired = 730,
                    maxDaysRequired = 819,
                    badgeIcon = drawableRes(R.drawable.badge_mountain),
                    message = stringRes(R.string.streak_item_two_year_titan_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_enlightened_one_title),
                    minDaysRequired = 820,
                    maxDaysRequired = 909,
                    badgeIcon = drawableRes(R.drawable.badge_star),
                    message = stringRes(R.string.streak_item_enlightened_one_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_cosmic_voyager_title),
                    minDaysRequired = 910,
                    maxDaysRequired = 999,
                    badgeIcon = drawableRes(R.drawable.badge_meteor),
                    message = stringRes(R.string.streak_item_cosmic_voyager_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_divine_habit_master_title),
                    minDaysRequired = 1000,
                    maxDaysRequired = 1094,
                    badgeIcon = drawableRes(R.drawable.badge_angel),
                    message = stringRes(R.string.streak_item_divine_habit_master_message),
                ),
                Streak(
                    title = stringRes(R.string.streak_item_habit_oracle_title),
                    minDaysRequired = 1095,
                    maxDaysRequired = Int.MAX_VALUE,
                    badgeIcon = drawableRes(R.drawable.badge_crystal_ball),
                    message = stringRes(R.string.streak_item_habit_oracle_message),
                ),
            )
        }
    }
