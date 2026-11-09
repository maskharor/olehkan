package com.olehkan.olehkan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.model.formattedPrice
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.HeartRed
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanShapes
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Orange
import com.olehkan.olehkan.ui.theme.StarGold
import com.olehkan.olehkan.ui.theme.White

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.98f)
                .clip(OlehKanShapes.Large)
                .background(Muted.copy(alpha = 0.1f))
        ) {
            AsyncImage(
                model = product.image,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )

            Surface(
                onClick = onFavoriteClick,
                shape = CircleShape,
                color = White.copy(alpha = 0.92f),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(9.dp)
                    .size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Hapus dari favorit"
                        else "Tambah ke favorit",
                        tint = if (isFavorite) HeartRed else Green,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(top = 10.dp, start = 2.dp, end = 2.dp)
        ) {
            Text(
                text = product.place.uppercase(),
                style = OlehKanType.ProductPlace,
                color = Orange,
            )

            Text(
                text = product.name,
                style = OlehKanType.ProductName,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = product.formattedPrice(),
                    style = OlehKanType.ProductPrice,
                    color = Green,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = " ${product.rating}",
                        style = OlehKanType.Caption,
                        color = Muted,
                    )
                }
            }
        }
    }
}