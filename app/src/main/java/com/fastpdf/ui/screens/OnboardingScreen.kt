package com.fastpdf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.ui.theme.GradientEnd
import com.fastpdf.ui.theme.GradientStart
import com.fastpdf.ui.theme.MintLight
import com.fastpdf.ui.theme.MintPale
import com.fastpdf.ui.theme.MintSoft
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.PrimaryLight
import com.fastpdf.ui.theme.PrimaryMedium
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Onboarding Screen — Matching the reference "PDF Reader, Read All Docs" UI.
 *
 * Features a beautiful mint gradient background with floating document icons,
 * a clean rounded title card, and a prominent "Get Started" CTA button.
 */

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Filled.PictureAsPdf,
        title = "PDF Reader, Read\nAll Docs",
        subtitle = "Read, manage & organize all your PDFs\neasily in one place.",
        gradient = listOf(Color(0xFF1A3C40), Color(0xFF2D6A6F))
    ),
    OnboardingPage(
        icon = Icons.Filled.Build,
        title = "Powerful Tools\nat Your Fingertips",
        subtitle = "Merge, Split, Compress, OCR, and Convert\n— all offline, all free.",
        gradient = listOf(Color(0xFF1A3C40), Color(0xFF2D6A6F))
    ),
    OnboardingPage(
        icon = Icons.Filled.AutoAwesome,
        title = "AI-Powered\nDocument Intelligence",
        subtitle = "Summarize documents, extract key points,\nand chat with your files.",
        gradient = listOf(Color(0xFF1A3C40), Color(0xFF2D6A6F))
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MintSoft, MintLight, MintPale),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onComplete) {
                        Text(
                            "Skip",
                            color = Primary.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = onboardingPages[page],
                    pageIndex = page
                )
            }

            // Page indicators
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 28f else 8f,
                        animationSpec = tween(300),
                        label = "indicator"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(width.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Primary else Primary.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            // "Get Started" CTA button — matching screenshot dark teal
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 48.dp)
                    .height(58.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Primary.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Floating document icons cluster
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Title card — white rounded rectangle like in screenshot
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Primary.copy(alpha = 0.15f)
                        )
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 28.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp,
                        color = Primary
                    )
                }

                // Floating document icons around the card
                // PDF icon — top right
                FloatingDocIcon(
                    icon = Icons.Filled.PictureAsPdf,
                    tint = Color(0xFFE53935),
                    bgColor = Color(0xFFFFEBEE),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 8.dp)
                        .graphicsLayer { rotationZ = 12f }
                )
                // Word icon — top left
                FloatingDocIcon(
                    icon = Icons.Filled.Description,
                    tint = Color(0xFF1565C0),
                    bgColor = Color(0xFFE3F2FD),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 20.dp)
                        .graphicsLayer { rotationZ = -8f }
                )
                // Excel icon — bottom left
                FloatingDocIcon(
                    icon = Icons.Filled.TableChart,
                    tint = Color(0xFF2E7D32),
                    bgColor = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 16.dp)
                        .graphicsLayer { rotationZ = 15f }
                )
                // PPT icon — bottom right
                FloatingDocIcon(
                    icon = Icons.Filled.Slideshow,
                    tint = Color(0xFFE65100),
                    bgColor = Color(0xFFFFF3E0),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 8.dp)
                        .graphicsLayer { rotationZ = -10f }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Subtitle text
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { 30 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "✨ ${page.subtitle}",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = Primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingDocIcon(
    icon: ImageVector,
    tint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = tint.copy(alpha = 0.2f)
            )
            .size(48.dp)
            .background(bgColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
    }
}
