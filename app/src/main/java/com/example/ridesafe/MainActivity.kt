package com.example.ridesafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ridesafe.data.database.RideEventDao
import com.example.ridesafe.data.database.RideSafeDbHelper
import com.example.ridesafe.data.database.TripDao
import com.example.ridesafe.data.repository.RideRepository
import com.example.ridesafe.ui.screens.DashboardScreen
import com.example.ridesafe.ui.screens.SettingsScreen
import com.example.ridesafe.ui.screens.TripDetailScreen
import com.example.ridesafe.ui.screens.TripHistoryScreen
import com.example.ridesafe.ui.theme.CockpitCyan
import com.example.ridesafe.ui.theme.RideSafeTheme
import com.example.ridesafe.viewmodel.RideViewModel
import com.example.ridesafe.viewmodel.RideViewModelFactory

class MainActivity : ComponentActivity() {

    private val dbHelper by lazy { RideSafeDbHelper(applicationContext) }
    private val tripDao by lazy { TripDao(dbHelper) }
    private val eventDao by lazy { RideEventDao(dbHelper) }
    private val repository by lazy { RideRepository(tripDao, eventDao) }

    private val viewModel: RideViewModel by viewModels {
        RideViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkModeOverride by viewModel.darkModeOverride.collectAsState()
            val lightStatus by viewModel.lightStatus.collectAsState()
            val systemDark = isSystemInDarkTheme()

            // Dynamic Dark Mode: if user hasn't overridden, follow Light Sensor (< 50 Lux = dark tunnel)
            val isDarkTheme = when (darkModeOverride) {
                true -> true
                false -> false
                null -> lightStatus.isTunnelOrDark || systemDark
            }

            RideSafeTheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: RideViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val selectedTrip by viewModel.selectedTrip.collectAsState()
    val selectedTripEvents by viewModel.selectedTripEvents.collectAsState()

    // Handle system back button when in detail view
    if (selectedTrip != null) {
        BackHandler {
            viewModel.clearSelectedTrip()
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedTrip == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Speed, contentDescription = "Dasbor") },
                        label = { Text("Dasbor", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.background,
                            indicatorColor = CockpitCyan
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                        label = { Text("Riwayat", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.background,
                            indicatorColor = CockpitCyan
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Sensors, contentDescription = "Sensor") },
                        label = { Text("Sensor", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.background,
                            indicatorColor = CockpitCyan
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (selectedTrip != null) {
                TripDetailScreen(
                    trip = selectedTrip!!,
                    events = selectedTripEvents,
                    onBack = { viewModel.clearSelectedTrip() }
                )
            } else {
                when (selectedTab) {
                    0 -> DashboardScreen(viewModel = viewModel)
                    1 -> TripHistoryScreen(viewModel = viewModel)
                    2 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
