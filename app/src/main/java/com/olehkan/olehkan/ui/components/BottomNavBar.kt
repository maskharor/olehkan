package com.olehkan.olehkan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.ui.navigation.bottomNavItems
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Paper

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Paper)
    ) {
        HorizontalDivider(
            color = Line,
            thickness = 1.dp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.BottomNavHeight)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                val icon = if (selected) item.iconSelected else item.iconUnselected
                val tint = if (selected) MaterialTheme.colorScheme.primary else Muted

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = item.label,
                        style = OlehKanType.BottomNavLabel,
                        color = tint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}