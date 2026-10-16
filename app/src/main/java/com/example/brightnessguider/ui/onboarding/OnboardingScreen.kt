package com.example.brightnessguider.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.brightnessguider.ui.theme.iOSBlue
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(page)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page Indicator (simplified)
            Row {
                repeat(3) { index ->
                    val color = if (pagerState.currentPage == index) iOSBlue else iOSBlue.copy(alpha = 0.2f)
                    Surface(
                        modifier = Modifier.padding(4.dp).size(8.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = color
                    ) {}
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = iOSBlue),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text(if (pagerState.currentPage < 2) "NEXT" else "GET STARTED", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    val title = when(page) {
        0 -> "Point at your monitor"
        1 -> "Smart light detection"
        else -> "Your eyes will thank you"
    }
    val sub = when(page) {
        0 -> "Hold your phone between you and your screen, camera facing the monitor"
        1 -> "Lumora compares your room light to your monitor to find the perfect brightness"
        else -> "Get real-time brightness and distance guidance tailored to your environment"
    }
    
    // Placeholder Lottie Animations (using generic ones or specific if available)
    // For now using composition from resources or placeholders
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(280.dp)) {
            // In a real app, you'd add: LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever)
            Text("Lottie Animation Placeholder", color = iOSBlue.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.Center))
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = sub,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
