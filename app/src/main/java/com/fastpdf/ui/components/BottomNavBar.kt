package com.fastpdf.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.navigation.bottomNavItems
import com.fastpdf.ui.theme.NavBarUnselected
import com.fastpdf.ui.theme.Primary
import com.fastpdf.ui.theme.PrimaryMedium

/**
 * Bottom navigation bar with a raised center Scan button.
 * Layout: [Home] [Tools] [SCAN ↑] [Files] [Profile]
 *
 * The scan button is elevated above the bar as a prominent circular FAB,
 * matching the reference "PDF Reader" UI pattern.
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onScanClick: () -> Unit = {}
) {
    // Split items: first 2 (Home, Tools) and last 2 (Files, Profile)
    val leftItems = bottomNavItems.take(2)
    val rightItems = bottomNavItems.drop(2)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ━━━ Bottom bar background with proper inset handling ━━━
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                // Left side tabs
                leftItems.forEach { item ->
                    NavTabItem(
                        label = item.label,
                        selectedIcon = item.selectedIcon,
                        unselectedIcon = item.unselectedIcon,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Center spacer for the raised scan button
                Spacer(modifier = Modifier.weight(1f))

                // Right side tabs
                rightItems.forEach { item ->
                    NavTabItem(
                        label = item.label,
                        selectedIcon = item.selectedIcon,
                        unselectedIcon = item.unselectedIcon,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // System navigation bar spacer — ensures content is above gesture bar
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )
        }

        // ━━━ Raised center Scan button (floats above the bar) ━━━
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = Primary.copy(alpha = 0.4f)
                    )
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Primary, PrimaryMedium)
                        )
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onScanClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DocumentScanner,
                    contentDescription = "Scan",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scan",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }
    }
}

/**
 * Single navigation tab item — icon + label stacked vertically.
 */
@Composable
private fun NavTabItem(
    label: String,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Primary else NavBarUnselected,
        animationSpec = tween(300),
        label = "nav_icon_color"
    )

    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconColor
        )
    }
}
