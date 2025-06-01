package com.shenawynkov.cities.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shenawynkov.cities.domain.model.City
import com.shenawynkov.cities.domain.model.Coordinates

// --- Styling Constants ---
private val ScreenBackgroundColor = Color(0xFFF5F5F7)
private val TopAppBarTitleColor = Color.Black // Matches image more closely than onSurface
private val TimelineLineColor = Color(0xFFE0E0E0)
private val TimelineGutterWidth = 32.dp
private val CityDotRadius = 5.dp // Results in a 10.dp dot
private val CityDotColor = Color(0xFFB0B0B0) // Grey dot
private val GroupHeaderHeight = 48.dp
private val GroupHeaderCircleSize = 30.dp
private val GroupHeaderCircleBackgroundColor = Color(0xFFECECEC) // Light grey circle
private val GroupHeaderCircleBorderColor = Color(0xFFDCDCDC) // Slightly darker border
private val GroupHeaderLetterColor = Color(0xFF757575) // Muted letter color

private val CityCardHeight = 120.dp
private val CityCardCornerRadius = 20.dp
private val CityCardFlagSize = 50.dp
private val FlagEmojiFontSize = 28.sp // Adjusted for emoji rendering in the circle
private val BottomSearchBarBackgroundColor = Color.White // For the search bar Surface
private val SearchBarInputBackgroundColor = Color(0xFFEFEFF0) // Added for the search field's internal background

// Constants for the timeline end dot
private val TimelineEndDotColor = Color(0xFFB0B0B0) // Grey dot, similar to previous CityDotColor
private val TimelineEndDotRadius = 5.dp          // Results in a 10.dp diameter dot
private val TimelineFooterHeight = 30.dp         // Height of the space for the end dot

// --- Helper function to convert country code to Emoji flag ---
fun countryCodeToEmojiFlag(countryCode: String): String {
    if (countryCode.length != 2) {
        return "❓" // Return a question mark or empty string for invalid codes
    }
    val codePoints = countryCode.uppercase().map {
        // Regional Indicator Symbol Letter A is 0x1F1E6
        // Each letter of the country code is an offset from this.
        0x1F1E6 + (it.code - 'A'.code)
    }
    return String(codePoints.toIntArray(), 0, codePoints.size)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    groupedCities: Map<Char, List<City>>,
    cityCount: Int,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    Scaffold(
        containerColor = ScreenBackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "City Search",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TopAppBarTitleColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TopAppBarTitleColor
                )
            )
        },
        bottomBar = {
            Surface(
                color = BottomSearchBarBackgroundColor,
                shadowElevation = 8.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search Icon")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SearchBarInputBackgroundColor,
                        unfocusedContainerColor = SearchBarInputBackgroundColor
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                text = "$cityCount cities",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                    }
                }
                groupedCities.isEmpty() && searchQuery.isNotEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No cities found for \"$searchQuery\"", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                    }
                }
                groupedCities.isEmpty() && searchQuery.isBlank() && !isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No cities to display. Try a search or check your data.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    ) {
                        groupedCities.forEach { (letter, citiesInGroup) ->
                            item(key = "header_$letter") {
                                TimelineGroupHeader(letter = letter)
                            }
                            items(citiesInGroup, key = { city -> "city_${city.id.toString()}" }) { city ->
                                TimelineCityRow(city = city)
                            }
                        }
                        // Add the TimelineFooter if there are any cities displayed
                        if (groupedCities.isNotEmpty()) {
                            item(key = "timeline_footer") {
                                TimelineFooter()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineGroupHeader(letter: Char) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(GroupHeaderHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(TimelineGutterWidth)
                .fillMaxHeight()
                .drawBehind {
                    val centerX = size.width / 2
                    drawLine(
                        color = TimelineLineColor,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = GroupHeaderCircleBackgroundColor,
                        radius = GroupHeaderCircleSize.toPx() / 2,
                        center = Offset(centerX, size.height / 2)
                    )
                    drawCircle(
                        color = GroupHeaderCircleBorderColor,
                        radius = GroupHeaderCircleSize.toPx() / 2,
                        center = Offset(centerX, size.height / 2),
                        style = Stroke(width = 1.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter.toString(),
                color = GroupHeaderLetterColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TimelineCityRow(city: City) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CityCardHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(TimelineGutterWidth)
                .fillMaxHeight()
                .drawBehind {
                    val centerX = size.width / 2f
                    drawLine(
                        color = TimelineLineColor,
                        start = Offset(x = centerX, y = 0f),
                        end = Offset(x = centerX, y = size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
        )
        CityCard(city = city, modifier = Modifier.weight(1f).padding(start = 8.dp, top = 16.dp))
    }
}

@Composable
fun TimelineFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TimelineFooterHeight), // Height for the footer space
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(TimelineGutterWidth)
                .fillMaxHeight()
                .drawBehind {
                    val centerX = size.width / 2f
                    val dotCenterY = size.height / 2f // Vertically centered dot

                    // Line from top of the footer item to the center of the dot
                    drawLine(
                        color = TimelineLineColor,
                        start = Offset(x = centerX, y = 0f),
                        end = Offset(x = centerX, y = dotCenterY), // Line stops at dot's center
                        strokeWidth = 2.dp.toPx()
                    )

                    // Draw the filled circle
                    drawCircle(
                        color = TimelineEndDotColor,
                        radius = TimelineEndDotRadius.toPx(),
                        center = Offset(centerX, dotCenterY)
                    )
                }
        )
        // Spacer to fill the rest of the width, as there's no card here
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun CityCard(city: City, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(CityCardHeight),
        shape = RoundedCornerShape(CityCardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(CityCardFlagSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countryCodeToEmojiFlag(city.country),
                    fontSize = FlagEmojiFontSize,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${city.name}, ${city.country.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lat: %.4f, Lon: %.4f".format(city.coord.lat, city.coord.lon),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- Previews ---
private fun createPreviewCity(id: Int, name: String, countryCode: String, lat: Double, lon: Double): City {
    return City(id = id, name = name, country = countryCode, coord = Coordinates(lat = lat, lon = lon))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun CityListScreenPreview_Populated() {
    val previewCities = listOf(
        createPreviewCity(id = 1, name = "Aabenraa", countryCode = "DK", lat = 55.0443, lon = 9.4174),
        createPreviewCity(id = 2, name = "Aalborg", countryCode = "DK", lat = 57.0488, lon = 9.9177),
        createPreviewCity(id = 3, name = "Berlin", countryCode = "DE", lat = 52.5200, lon = 13.4050),
        createPreviewCity(id = 4, name = "Zaamslag", countryCode = "NL", lat = 51.3125, lon = 3.9125)
    )
    MaterialTheme {
        CityListScreen(
            searchQuery = "",
            onSearchQueryChanged = {},
            groupedCities = previewCities.groupBy { it.name.first().uppercaseChar() }.toSortedMap(),
            cityCount = previewCities.size,
            isLoading = false,
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun CityListScreenPreview_Loading() {
    MaterialTheme {
        CityListScreen(
            searchQuery = "",
            onSearchQueryChanged = {},
            groupedCities = emptyMap(),
            cityCount = 0,
            isLoading = true,
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun CityListScreenPreview_Error() {
    MaterialTheme {
        CityListScreen(
            searchQuery = "Test",
            onSearchQueryChanged = {},
            groupedCities = emptyMap(),
            cityCount = 0,
            isLoading = false,
            errorMessage = "Failed to load cities. Please try again."
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun CityListScreenPreview_NoResults() {
    MaterialTheme {
        CityListScreen(
            searchQuery = "NonExistentCity",
            onSearchQueryChanged = {},
            groupedCities = emptyMap(),
            cityCount = 0,
            isLoading = false,
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TimelineFooterPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(ScreenBackgroundColor).padding(16.dp)) {
            TimelineFooter()
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TimelineGroupHeaderPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(ScreenBackgroundColor).padding(16.dp)) {
            TimelineGroupHeader(letter = 'A')
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TimelineCityRowPreview() {
    val previewCity = createPreviewCity(id = 1, name = "Aabenraa", countryCode = "DK", lat = 55.0443, lon = 9.4174)
    MaterialTheme {
         Box(modifier = Modifier.background(ScreenBackgroundColor).padding(16.dp)) {
            TimelineCityRow(city = previewCity)
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun CityCardPreview() {
    val previewCity = createPreviewCity(id = 1, name = "Aabenraa", countryCode = "DK", lat = 55.0443, lon = 9.4174)
    MaterialTheme {
        Box(modifier = Modifier.background(ScreenBackgroundColor).padding(16.dp)) {
             CityCard(city = previewCity)
        }
    }
} 