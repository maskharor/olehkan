package com.olehkan.olehkan.ui.screens.reviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.model.RatingSummary
import com.olehkan.olehkan.data.model.Review
import com.olehkan.olehkan.data.model.SellerResponse
import com.olehkan.olehkan.data.model.formattedPrice
import com.olehkan.olehkan.data.repository.DummyReviewRepository
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenDark
import com.olehkan.olehkan.ui.theme.GreenLight
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanShapes
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Orange
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.SearchPlaceholder
import com.olehkan.olehkan.ui.theme.StarGold
import com.olehkan.olehkan.ui.theme.WarningBg
import com.olehkan.olehkan.ui.theme.White
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ReviewsScreen(
    productId: Int?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewsViewModel = viewModel(),
) {
    LaunchedEffect(productId) {
        viewModel.load(productId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearUserMessage()
            }
        }
    }

    ReviewsContent(
        uiState = uiState,
        onBack = onBack,
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onOpenWriteReview = viewModel::openWriteReviewSheet,
        onCloseWriteReview = viewModel::closeWriteReviewSheet,
        onSetRating = viewModel::setNewReviewRating,
        onSetComment = viewModel::setNewReviewComment,
        onToggleTag = viewModel::toggleNewReviewTag,
        onSubmitReview = viewModel::submitReview,
        onToggleHelpful = viewModel::toggleHelpful,
        onRetry = { viewModel.load(productId) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsContent(
    uiState: ReviewsUiState,
    onBack: () -> Unit,
    onFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onOpenWriteReview: () -> Unit,
    onCloseWriteReview: () -> Unit,
    onSetRating: (Int) -> Unit,
    onSetComment: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onSubmitReview: () -> Unit,
    onToggleHelpful: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Paper,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Top Bar
            Surface(
                color = Paper,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Ink,
                            )
                        }

                        Text(
                            text = if (uiState.productId != null) "Ulasan Produk" else "Semua Ulasan",
                            style = OlehKanType.SubHeaderTitle,
                            color = Ink,
                        )

                        IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = "Cari ulasan",
                                tint = Ink,
                            )
                        }
                    }

                    // Search input expansion
                    AnimatedVisibility(visible = isSearchExpanded) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    "Cari kata kunci dalam ulasan...",
                                    style = OlehKanType.BodySmall,
                                    color = SearchPlaceholder
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null, tint = Muted)
                            },
                            singleLine = true,
                            shape = OlehKanShapes.Pill,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Line,
                                focusedBorderColor = Green,
                                unfocusedContainerColor = White,
                                focusedContainerColor = White,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.ScreenPadding, vertical = 4.dp)
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Green)
                    }
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("Gagal Memuat Ulasan", style = OlehKanType.H3, color = Ink)
                        Text(
                            text = uiState.errorMessage,
                            style = OlehKanType.BodySmall,
                            color = Muted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        TextButton(onClick = onRetry) {
                            Text("Coba Lagi", color = Green)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Product Context Header Banner (if viewing specific product)
                        if (uiState.product != null) {
                            item {
                                ProductHeaderBanner(product = uiState.product)
                            }
                        }

                        // Rating Overview Summary Card
                        item {
                            RatingSummarySection(
                                summary = uiState.ratingSummary,
                                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                            )
                        }

                        // Filter Chips Row
                        item {
                            Column {
                                Text(
                                    text = "FILTER ULASAN",
                                    style = OlehKanType.Eyebrow,
                                    color = Orange,
                                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 4.dp)
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    items(ReviewsUiState.filterOptions) { filter ->
                                        val selected = filter == uiState.selectedFilter
                                        Box(
                                            modifier = Modifier
                                                .clip(OlehKanShapes.Pill)
                                                .background(if (selected) Green else White)
                                                .clickable { onFilterSelected(filter) }
                                                .padding(horizontal = 16.dp, vertical = 9.dp)
                                        ) {
                                            Text(
                                                text = filter,
                                                style = OlehKanType.ChipLabel,
                                                color = if (selected) White else Muted,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Reviews Count Header & Action
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Menampilkan ${uiState.filteredReviews.size} Ulasan",
                                    style = OlehKanType.H3,
                                    color = Ink,
                                )

                                OutlinedButton(
                                    onClick = onOpenWriteReview,
                                    shape = OlehKanShapes.Pill,
                                    border = BorderStroke(1.dp, Green),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RateReview,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tulis Ulasan", style = OlehKanType.CaptionBold)
                                }
                            }
                        }

                        // List of Customer Reviews
                        if (uiState.filteredReviews.isEmpty()) {
                            item {
                                EmptyReviewsPlaceholder(searchQuery = uiState.searchQuery)
                            }
                        } else {
                            items(uiState.filteredReviews, key = { it.id }) { review ->
                                ReviewItemCard(
                                    review = review,
                                    onToggleHelpful = { onToggleHelpful(review.id) },
                                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modal Bottom Sheet for Writing Customer Review
        if (uiState.isWritingReviewSheetOpen) {
            WriteReviewBottomSheet(
                product = uiState.product,
                rating = uiState.newReviewRating,
                comment = uiState.newReviewComment,
                selectedTags = uiState.selectedNewTags,
                isSubmitting = uiState.isSubmitting,
                onRatingChanged = onSetRating,
                onCommentChanged = onSetComment,
                onTagToggled = onToggleTag,
                onDismiss = onCloseWriteReview,
                onSubmit = onSubmitReview,
            )
        }
    }
}

@Composable
private fun ProductHeaderBanner(product: Product) {
    Surface(
        color = White,
        border = BorderStroke(1.dp, Line),
        shape = OlehKanShapes.Large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(OlehKanShapes.Medium)
                    .background(Muted.copy(alpha = 0.1f))
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = product.place.uppercase(),
                    style = OlehKanType.ProductPlace,
                    color = Orange,
                )
                Text(
                    text = product.name,
                    style = OlehKanType.ProductName.copy(fontSize = 14.sp),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = product.formattedPrice(),
                        style = OlehKanType.ProductPrice,
                        color = Green
                    )
                    Text(
                        text = "• ${product.category}",
                        style = OlehKanType.Caption,
                        color = Muted
                    )
                }
            }

            Surface(
                shape = OlehKanShapes.Pill,
                color = WarningBg,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${product.rating}",
                        style = OlehKanType.CaptionBold,
                        color = Ink
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RatingSummarySection(
    summary: RatingSummary,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = White,
        shape = OlehKanShapes.Large,
        border = BorderStroke(1.dp, Line),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "PENILAIAN PELANGGAN",
                        style = OlehKanType.Eyebrow,
                        color = Orange
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ringkasan Ulasan",
                        style = OlehKanType.H2,
                        color = Ink
                    )
                }

                Surface(
                    shape = OlehKanShapes.Pill,
                    color = GreenLight,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${summary.satisfactionPercentage}% Pembeli Puas",
                            style = OlehKanType.CaptionBold,
                            color = GreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Left column: Rating score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(110.dp)
                ) {
                    val formattedAvg = String.format(Locale.getDefault(), "%.1f", summary.averageRating).replace(",", ".")
                    Text(
                        text = formattedAvg,
                        style = OlehKanType.RatingBig,
                        color = Ink
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starRating = index + 1
                            Icon(
                                imageVector = if (starRating <= summary.averageRating) Icons.Filled.Star
                                else Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "${summary.totalReviews} ulasan",
                        style = OlehKanType.Caption,
                        color = Muted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(80.dp)
                        .background(Line)
                )

                // Right column: Rating bars (5 to 1 star)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (5 downTo 1).forEach { star ->
                        val count = summary.starCounts[star] ?: 0
                        val progress = if (summary.totalReviews > 0) count.toFloat() / summary.totalReviews else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "$star ★",
                                style = OlehKanType.CaptionBold,
                                color = Muted,
                                modifier = Modifier.width(22.dp)
                            )

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(OlehKanShapes.Pill),
                                color = StarGold,
                                trackColor = Line,
                            )

                            Text(
                                text = "$count",
                                style = OlehKanType.Caption,
                                color = Muted,
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Line, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Highlight Tags list
            Text(
                text = "Hal yang paling disukai pembeli:",
                style = OlehKanType.CaptionBold,
                color = Muted,
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                summary.topTags.forEach { (tag, count) ->
                    Surface(
                        shape = OlehKanShapes.Pill,
                        color = Paper,
                        border = BorderStroke(1.dp, Line),
                    ) {
                        Text(
                            text = "👍 $tag ($count)",
                            style = OlehKanType.Caption,
                            color = Ink,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItemCard(
    review: Review,
    onToggleHelpful: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = White,
        shape = OlehKanShapes.Large,
        border = BorderStroke(1.dp, Line),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(GreenLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.userName.take(1).uppercase(),
                            style = OlehKanType.SubHeaderTitle.copy(fontSize = 16.sp),
                            color = GreenDark,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = review.userName,
                                style = OlehKanType.BodyBold,
                                color = Ink
                            )

                            if (review.isVerifiedBuyer) {
                                Surface(
                                    shape = OlehKanShapes.Pill,
                                    color = Orange.copy(alpha = 0.12f),
                                ) {
                                    Text(
                                        text = "Pembeli Terverifikasi",
                                        style = OlehKanType.Tiny.copy(fontWeight = FontWeight.Bold),
                                        color = Orange,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = review.date,
                            style = OlehKanType.Caption,
                            color = Muted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rating Stars & Variant
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                review.variant?.let { variantText ->
                    Text(
                        text = "• $variantText",
                        style = OlehKanType.Caption,
                        color = Muted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Review Comment Body
            Text(
                text = review.comment,
                style = OlehKanType.Body,
                color = Ink,
                lineHeight = 20.sp,
            )

            // Review Photos Thumbnails
            if (review.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(review.images) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(OlehKanShapes.Medium)
                                .background(Muted.copy(alpha = 0.1f))
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Foto ulasan pembeli",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
            }

            // Quick Feedback Tags (if present)
            if (review.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    review.tags.forEach { tag ->
                        Surface(
                            shape = OlehKanShapes.Pill,
                            color = GreenLight,
                        ) {
                            Text(
                                text = "✓ $tag",
                                style = OlehKanType.Tiny,
                                color = GreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Seller Response Card
            review.sellerResponse?.let { response ->
                Spacer(modifier = Modifier.height(14.dp))
                SellerResponseBox(response = response)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Line, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Helpful action footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Apakah ulasan ini membantu?",
                    style = OlehKanType.Caption,
                    color = Muted
                )

                Surface(
                    onClick = onToggleHelpful,
                    shape = OlehKanShapes.Pill,
                    color = if (review.isHelpful) GreenLight else Paper,
                    border = BorderStroke(1.dp, if (review.isHelpful) Green else Line),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = if (review.isHelpful) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Bermanfaat",
                            tint = if (review.isHelpful) Green else Muted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (review.helpfulCount > 0) "Bermanfaat (${review.helpfulCount})" else "Bermanfaat",
                            style = OlehKanType.CaptionBold,
                            color = if (review.isHelpful) GreenDark else Muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerResponseBox(response: SellerResponse) {
    Surface(
        color = Paper,
        shape = OlehKanShapes.Medium,
        border = BorderStroke(1.dp, Green.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = response.sellerName,
                        style = OlehKanType.CaptionBold,
                        color = Ink
                    )
                }

                Text(
                    text = response.date,
                    style = OlehKanType.Tiny,
                    color = Muted
                )
            }

            Text(
                text = response.message,
                style = OlehKanType.BodySmall,
                color = Ink.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun EmptyReviewsPlaceholder(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (searchQuery.isNotBlank()) "Ulasan tidak ditemukan untuk '$searchQuery'"
                else "Belum ada ulasan untuk kategori ini.",
                style = OlehKanType.Body,
                color = Muted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WriteReviewBottomSheet(
    product: Product?,
    rating: Int,
    comment: String,
    selectedTags: Set<String>,
    isSubmitting: Boolean,
    onRatingChanged: (Int) -> Unit,
    onCommentChanged: (String) -> Unit,
    onTagToggled: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = OlehKanShapes.DetailSheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp)
        ) {
            Text(
                text = "Tulis Ulasan Produk",
                style = OlehKanType.SubHeaderTitle,
                color = Ink
            )
            Text(
                text = product?.name ?: "Bakpia Pathok Premium",
                style = OlehKanType.BodySmall,
                color = Muted
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Star Rating Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Berikan Penilaian Kamu",
                    style = OlehKanType.CaptionBold,
                    color = Muted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { starIndex ->
                        val isSelected = starIndex <= rating
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "$starIndex Star",
                            tint = StarGold,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onRatingChanged(starIndex) }
                        )
                    }
                }

                val ratingLabel = when (rating) {
                    5 -> "5 - Sangat Puas! 🎉"
                    4 -> "4 - Bagus Banget! 👍"
                    3 -> "3 - Cukup Baik 😊"
                    2 -> "2 - Kurang Memuaskan 🙁"
                    else -> "1 - Kecewa 😞"
                }

                Text(
                    text = ratingLabel,
                    style = OlehKanType.BodyBold,
                    color = Orange,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Feedback Tags Selector
            Text(
                text = "Pilih Keunggulan Produk (Opsional):",
                style = OlehKanType.CaptionBold,
                color = Muted
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ReviewsUiState.quickTags.forEach { tag ->
                    val isSelected = tag in selectedTags
                    Box(
                        modifier = Modifier
                            .clip(OlehKanShapes.Pill)
                            .background(if (isSelected) Green else Paper)
                            .clickable { onTagToggled(tag) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isSelected) "✓ $tag" else "+ $tag",
                            style = OlehKanType.CaptionBold,
                            color = if (isSelected) White else Muted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comment Input Field
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                placeholder = {
                    Text(
                        "Bagikan rasa, tekstur, atau pengalaman pengemasan untuk membantu pembeli lain...",
                        style = OlehKanType.BodySmall,
                        color = SearchPlaceholder
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = OlehKanShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Line,
                    focusedBorderColor = Green,
                    unfocusedContainerColor = Paper,
                    focusedContainerColor = Paper,
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Add Photo Attachment Mock Button
            OutlinedButton(
                onClick = { },
                shape = OlehKanShapes.Medium,
                border = BorderStroke(1.dp, Line),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Muted),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Foto / Video Produk", style = OlehKanType.ButtonPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting && comment.isNotBlank(),
                shape = OlehKanShapes.Medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Kirim Ulasan", style = OlehKanType.ButtonLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ReviewsScreenPreview() {
    OlehKanTheme {
        ReviewsContent(
            uiState = ReviewsUiState(
                isLoading = false,
                productId = 1,
                product = Product(
                    id = 1,
                    name = "Bakpia Pathok Premium",
                    place = "Yogyakarta",
                    price = 42_000,
                    rating = 4.9,
                    reviews = 128,
                    stock = 24,
                    category = "Makanan",
                    image = "https://images.unsplash.com/photo-1586511926871-deb1aca37f38?auto=format&fit=crop&w=800&q=85",
                    description = "Bakpia lembut khas Pathok.",
                ),
                reviews = DummyReviewRepository.initialDummyReviews,
                filteredReviews = DummyReviewRepository.initialDummyReviews,
                ratingSummary = RatingSummary(),
            ),
            onBack = {},
            onFilterSelected = {},
            onSearchQueryChanged = {},
            onOpenWriteReview = {},
            onCloseWriteReview = {},
            onSetRating = {},
            onSetComment = {},
            onToggleTag = {},
            onSubmitReview = {},
            onToggleHelpful = {},
            onRetry = {},
        )
    }
}
