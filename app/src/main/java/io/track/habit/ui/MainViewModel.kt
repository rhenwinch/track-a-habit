package io.track.habit.ui

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.ui.navigation.NavRoute.Companion.TOP_LEVEL_ROUTES
import io.track.habit.ui.navigation.TopLevelBackStack
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor() : ViewModel() {
        val backStack = TopLevelBackStack<NavKey>(startKey = TOP_LEVEL_ROUTES.first())
    }
