package com.olehkan.olehkan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.ui.screens.orders.OrderTab
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.White

/**
 * Tab switcher untuk OrdersScreen.
 * 2 tab: "Sedang diproses" & "Selesai".
 */
@Composable
fun OrderTabs(
    selected: OrderTab,
    activeCount: Int,
    onTabSelected: (OrderTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabItem(
            label = OrderTab.ACTIVE.label,
            count = activeCount.takeIf { it > 0 },
            selected = selected == OrderTab.ACTIVE,
            onClick = { onTabSelected(OrderTab.ACTIVE) },
            modifier = Modifier.weight(1f),
        )
        TabItem(
            label = OrderTab.COMPLETED.label,
            count = null,
            selected = selected == OrderTab.COMPLETED,
            onClick = { onTabSelected(OrderTab.COMPLETED) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabItem(
    label: String,
    count: Int?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Green else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = OlehKanType.TabLabel,
                color = if (selected) White else Muted,
                fontWeight = FontWeight.SemiBold,
            )
            if (count != null) {
                Text(
                    text = "  $count",
                    style = OlehKanType.CaptionBold,
                    color = if (selected) White.copy(alpha = 0.85f) else Muted,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}