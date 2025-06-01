package com.shenawynkov.cities.presentation.ui.screen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Styling Constants ---
internal val ScreenBackgroundColor = Color(0xFFF5F5F7)
internal val TopAppBarTitleColor = Color.Black // Matches image more closely than onSurface
internal val TimelineLineColor = Color(0xFFE0E0E0)
internal val TimelineGutterWidth = 32.dp

internal val GroupHeaderHeight = 48.dp
internal val GroupHeaderCircleSize = 48.dp
internal val GroupHeaderCircleBackgroundColor = Color.White // Changed from Color(0xFFECECEC) to Color.White
internal val GroupHeaderCircleBorderColor = Color(0xFFDCDCDC) // Slightly darker border
internal val GroupHeaderLetterColor = Color(0xFF1A1A1A) // Muted letter color

internal val CityCardHeight = 120.dp
internal val CityCardCornerRadius = 16.dp
internal val CityCardFlagSize = 50.dp
internal val FlagEmojiFontSize = 28.sp // Adjusted for emoji rendering in the circle
internal val BottomSearchBarBackgroundColor = Color.White // For the search bar Surface
internal val SearchBarInputBackgroundColor = Color(0xFFEFEFF0) // Added for the search field's internal background

// Constants for the timeline end dot
internal val TimelineEndDotColor = Color(0xFFB0B0B0) // Grey dot
internal val TimelineEndDotRadius = 8.dp          // Results in a 10.dp diameter dot
internal val TimelineFooterHeight = 30.dp         // Height of the space for the end dot 