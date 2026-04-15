package com.fastpdf.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.domain.model.StampType

/**
 * Stamp picker showing predefined stamps (APPROVED, REJECTED, etc.)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StampPicker(
    onStampSelect: (StampType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Stamp",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StampType.entries.forEach { stamp ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, stamp.color, RoundedCornerShape(8.dp))
                        .background(stamp.color.copy(alpha = 0.08f))
                        .clickable { onStampSelect(stamp) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stamp.label,
                        color = stamp.color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
