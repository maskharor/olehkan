package com.olehkan.olehkan.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.data.model.Order
import com.olehkan.olehkan.ui.components.OrderCard
import com.olehkan.olehkan.ui.components.OrderTabs
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Paper

@Composable
fun OrdersContent(
    uiState: OrdersUiState,
    onTabSelected: (OrderTab) -> Unit,
    onPayClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        // ===== HEADER =====
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Pesanan",
            style = OlehKanType.SubHeaderTitle,
            color = Ink,
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
        )

        // ===== TAB =====
        Spacer(Modifier.height(8.dp))
        OrderTabs(
            selected = uiState.selectedTab,
            activeCount = uiState.activeCount,
            onTabSelected = onTabSelected,
        )

        // ===== CONTENT =====
        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null -> ErrorState(uiState.errorMessage, onRetry)
            uiState.isCurrentTabEmpty -> EmptyState(uiState.selectedTab)
            else -> OrderList(
                orders = when (uiState.selectedTab) {
                    OrderTab.ACTIVE -> uiState.activeOrders
                    OrderTab.COMPLETED -> uiState.completedOrders
                },
                onPayClick = onPayClick,
            )
        }
    }
}

// ===== Sub-components =====

@Composable
private fun OrderList(
    orders: List<Order>,
    onPayClick: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(orders, key = { it.id }) { order ->
            OrderCard(
                order = order,
                onPayClick = onPayClick,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Green)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Gagal memuat", style = OlehKanType.H3, color = Ink)
        Text(
            message,
            style = OlehKanType.BodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        TextButton(onClick = onRetry) {
            Text("Coba lagi", color = Green)
        }
    }
}

@Composable
private fun EmptyState(tab: OrderTab) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (tab == OrderTab.ACTIVE) "Tidak ada pesanan aktif"
            else "Belum ada pesanan selesai",
            style = OlehKanType.Body,
            color = Muted,
            textAlign = TextAlign.Center,
        )
    }
}