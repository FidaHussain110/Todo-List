package uk.ac.tees.mad.E4294395

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme

// Splash screen displayed for 3 seconds before navigating to task list
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    LaunchedEffect(Unit) {
        delay(3000) // Wait 3 seconds
        onTimeout()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else Color.White), // Ensure background contrasts with text
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "To-Do List",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor // White in dark theme, black in light theme
        )
    }
}