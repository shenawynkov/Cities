package com.shenawynkov.cities.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shenawynkov.cities.domain.model.City
import com.shenawynkov.cities.domain.model.Coordinates
import com.shenawynkov.cities.presentation.ui.util.countryCodeToEmojiFlag
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable

import com.shenawynkov.cities.presentation.ui.util.slideInFromRightAndFadeOnEnter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CityListScreen(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    groupedCities: Map<Char, List<City>>,
    cityCount: Int,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onCityClick: (City) -> Unit
) {
    var isSearchBarFocused by remember { mutableStateOf(false) }

    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val localFocusManager = LocalFocusManager.current

    val activeSearchBarSurfaceColor by animateColorAsState(
        targetValue = if (isSearchBarFocused) BottomSearchBarBackgroundColor else SearchBarInputBackgroundColor,
        label = "ActiveSearchBarSurfaceColor"
    )

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
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .imePadding()
                    .animateContentSize(animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.background(Color.White),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = !isSearchActive,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        CollapsedSearchBar {
                            isSearchActive = true
                        }
                    }

                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ActiveSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChanged = onSearchQueryChanged,
                            focusRequester = focusRequester,
                            surfaceColor = activeSearchBarSurfaceColor,
                            onFocusChanged = { focused ->
                                isSearchBarFocused = focused
                            },
                            onCloseSearch = {
                                onSearchQueryChanged("")
                                localFocusManager.clearFocus()
                                isSearchActive = false
                                isSearchBarFocused = false
                            }
                        )
                    }
                }
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

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null && !isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = groupedCities.isEmpty() && searchQuery.isNotEmpty() && !isLoading && errorMessage == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No cities found for \"$searchQuery\"",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = groupedCities.isEmpty() && searchQuery.isBlank() && !isLoading && errorMessage == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No cities to display. Try a search or check your data.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (groupedCities.isNotEmpty() && !isLoading && errorMessage == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    groupedCities.forEach { (letter, citiesInGroup) ->
                        stickyHeader(key = "header_$letter") {
                            TimelineGroupHeader(
                                letter = letter,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                        items(
                            citiesInGroup,
                            key = { city -> "city_${city.id.toString()}" }) { city ->
                            TimelineCityRow(
                                city = city,
                                onCityClick = onCityClick,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
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

@Composable
fun TimelineGroupHeader(letter: Char, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(GroupHeaderHeight)
            .background(ScreenBackgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(TimelineGutterWidth)
                .fillMaxHeight()
                .drawBehind {
                    val centerX = size.width / 2
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
fun TimelineCityRow(city: City, onCityClick: (City) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
        CityCard(
            city = city,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 4.dp, top = 16.dp, bottom = 8.dp)
                .slideInFromRightAndFadeOnEnter(),
            onCityClick = onCityClick
        )
    }
}

@Composable
fun TimelineFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TimelineFooterHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(TimelineGutterWidth)
                .fillMaxHeight()
                .drawBehind {
                    val centerX = size.width / 2f
                    val dotCenterY = size.height / 2f

                    drawLine(
                        color = TimelineLineColor,
                        start = Offset(x = centerX, y = 0f),
                        end = Offset(x = centerX, y = dotCenterY),
                        strokeWidth = 2.dp.toPx()
                    )

                    drawCircle(
                        color = TimelineEndDotColor,
                        radius = TimelineEndDotRadius.toPx(),
                        center = Offset(centerX, dotCenterY)
                    )
                }
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun CityCard(city: City, modifier: Modifier = Modifier, onCityClick: (City) -> Unit) {
        Card(
        modifier = modifier
            .fillMaxWidth()
            .height(CityCardHeight)
            .clickable { onCityClick(city) },
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

@Composable
private fun CollapsedSearchBar(onActivateSearch: () -> Unit) {
    Surface(
        color = SearchBarInputBackgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 28.dp, start = 16.dp, end = 16.dp)
            .height(40.dp)
            .clickable { onActivateSearch() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray
            )
            Spacer(Modifier.width(8.dp))
            Text("Search...", color = Color.Gray)
        }
    }
}

@Composable
private fun ActiveSearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    focusRequester: FocusRequester,
    surfaceColor: Color,
    onFocusChanged: (Boolean) -> Unit,
    onCloseSearch: () -> Unit
) {
    Surface(
        color = surfaceColor,
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState -> onFocusChanged(focusState.isFocused) }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            placeholder = { Text("Search...") },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search Icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(36.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        onSearchQueryChanged("")
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear Search",
                            tint = Color.Gray
                        )
                    }
                } else {
                    IconButton(onClick = onCloseSearch) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close Search",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                cursorColor = TopAppBarTitleColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp)
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// --- Previews ---
private fun createPreviewCity(
    id: Int,
    name: String,
    countryCode: String,
    lat: Double,
    lon: Double
): City {
    return City(
        id = id,
        name = name,
        country = countryCode,
        coord = Coordinates(lat = lat, lon = lon)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun CityListScreenPreview_Populated() {
    val previewCities = listOf(
        createPreviewCity(
            id = 1,
            name = "Aabenraa",
            countryCode = "DK",
            lat = 55.0443,
            lon = 9.4174
        ),
        createPreviewCity(
            id = 2,
            name = "Aalborg",
            countryCode = "DK",
            lat = 57.0488,
            lon = 9.9177
        ),
        createPreviewCity(
            id = 3,
            name = "Berlin",
            countryCode = "DE",
            lat = 52.5200,
            lon = 13.4050
        ),
        createPreviewCity(
            id = 4,
            name = "Zaamslag",
            countryCode = "NL",
            lat = 51.3125,
            lon = 3.9125
        )
    )
    MaterialTheme {
        CityListScreen(
            searchQuery = "",
            onSearchQueryChanged = {},
            groupedCities = previewCities.groupBy { it.name.first().uppercaseChar() }.toSortedMap(),
            cityCount = previewCities.size,
            isLoading = false,
            errorMessage = null,
            onCityClick = {}
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
            errorMessage = null,
            onCityClick = {}
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
            errorMessage = "Failed to load cities. Please try again.",
            onCityClick = {}
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
            errorMessage = null,
            onCityClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TimelineFooterPreview() {
    MaterialTheme {
        Box(modifier = Modifier
            .background(ScreenBackgroundColor)
            .padding(16.dp)) {
            TimelineFooter()
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TimelineGroupHeaderPreview() {
    MaterialTheme {
        Box(modifier = Modifier
            .background(ScreenBackgroundColor)
            .padding(16.dp)) {
            TimelineGroupHeader(letter = 'A')
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TimelineCityRowPreview() {
    val previewCity = createPreviewCity(
        id = 1,
        name = "Aabenraa",
        countryCode = "DK",
        lat = 55.0443,
        lon = 9.4174
    )
    MaterialTheme {
        Box(modifier = Modifier
            .background(ScreenBackgroundColor)
            .padding(16.dp)) {
            TimelineCityRow(city = previewCity, onCityClick = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun CityCardPreview() {
    val previewCity = createPreviewCity(
        id = 1,
        name = "Aabenraa",
        countryCode = "DK",
        lat = 55.0443,
        lon = 9.4174
    )
    MaterialTheme {
        Box(modifier = Modifier
            .background(ScreenBackgroundColor)
            .padding(16.dp)) {
            CityCard(city = previewCity, onCityClick = {})
        }
    }
} 