package com.shenawynkov.cities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.shenawynkov.cities.presentation.ui.screen.CityListScreen
import com.shenawynkov.cities.presentation.ui.theme.CitiesTheme
import com.shenawynkov.cities.presentation.viewmodel.CityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CitiesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CityApp()
                }
            }
        }
    }
}

@Composable
fun CityApp(
    viewModel: CityViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val groupedCities by viewModel.groupedCities.collectAsState()
    val cityCount by viewModel.cityCount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    CityListScreen(
        searchQuery = searchText,
        onSearchQueryChanged = viewModel::onSearchTextChanged,
        groupedCities = groupedCities,
        cityCount = cityCount,
        isLoading = isLoading,
        errorMessage = errorMessage
    )
}

// Greeting and GreetingPreview can be removed if no longer needed, or kept for other previews.
// For this task, they are not essential for the main app flow. 