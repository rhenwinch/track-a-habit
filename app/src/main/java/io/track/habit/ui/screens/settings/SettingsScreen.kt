package io.track.habit.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.track.habit.R
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.GeneralSettingsRegistry
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.ui.screens.settings.components.SettingItem
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val generalSettings by viewModel.general.collectAsState()

    SettingsScreenContent(
        generalSettings = generalSettings,
        onSettingChange = { definition, value ->
            viewModel.updateSettingWithCast(definition, value)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    generalSettings: GeneralSettings,
    onSettingChange: (definition: SettingDefinition<*>, value: Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Filter out lastShowcasedHabitId from the settings list
            val settingDefinitions =
                GeneralSettingsRegistry.getAllSettings().filter {
                    it != GeneralSettingsRegistry.LAST_SHOWCASED_HABIT_ID
                }

            settingDefinitions.forEach { definition ->
                when (definition) {
                    GeneralSettingsRegistry.USER_NAME -> {
                        SettingItem(
                            definition = definition,
                            currentValue = generalSettings.userName,
                            onValueChange = {
                                onSettingChange(definition, it)
                            },
                        )
                    }
                    GeneralSettingsRegistry.CENSOR_HABIT_NAMES -> {
                        SettingItem(
                            definition = definition,
                            currentValue = generalSettings.censorHabitNames,
                            onValueChange = { onSettingChange(definition, it) },
                        )
                    }
                    GeneralSettingsRegistry.LOCK_RESET_PROGRESS -> {
                        SettingItem(
                            definition = definition,
                            currentValue = generalSettings.lockResetProgressButton,
                            onValueChange = { onSettingChange(definition, it) },
                        )
                    }
                    GeneralSettingsRegistry.NOTIFICATIONS_ENABLED -> {
                        SettingItem(
                            definition = definition,
                            currentValue = generalSettings.notificationsEnabled,
                            onValueChange = { onSettingChange(definition, it) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    TrackAHabitTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsScreenContent(
                generalSettings =
                    GeneralSettings(
                        userName = "John Doe",
                        censorHabitNames = true,
                        lockResetProgressButton = false,
                        notificationsEnabled = true,
                    ),
                onSettingChange = { _, _ -> },
            )
        }
    }
}
