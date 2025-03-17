package com.apero.core.ads.ui.native.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.apero.core.ads.ui.native.NativeAdLayoutScope
import com.apero.core.ads.ui.native.PreviewNativeAdLayout
import com.valentinilk.shimmer.shimmer
import kotlin.math.roundToInt

internal const val MAX_RATING = 5

@Composable
public fun NativeAdLayoutScope.StarRating(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    tintColor: Color = Color(0xFFFDB812),
    fullStarIcon: @Composable () -> Unit = {
        Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = tintColor)
    },
    halfStarIcon: @Composable () -> Unit = {
        Icon(imageVector = Icons.Outlined.StarHalf, contentDescription = null, tint = tintColor)
    },
    emptyStarIcon: @Composable () -> Unit = {
        Icon(imageVector = Icons.Outlined.StarOutline, contentDescription = null, tint = tintColor)
    },
) {
    if (adUiState.isReady && adUiState.starRating == null) return

    NativeAdElementDelegate(
        onBindDelegate = {
            nativeAdView.starRatingView = it
        }
    ) {
        val shimmerModifier = if (adUiState.starRating == null) {
            Modifier
                .shimmer()
                .background(Color.LightGray)
        } else {
            Modifier
        }
        val rating = (adUiState.starRating ?: 0.0).coerceAtLeast(0.0)
        val fullStars = rating.toInt()
        val halfStar = rating.roundToInt() - fullStars
        val emptyStars = (MAX_RATING - halfStar - fullStars).coerceAtLeast(0)
        Row(
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            modifier = shimmerModifier
                .then(modifier)
        ) {
            repeat(fullStars) {
                fullStarIcon()
            }
            repeat(halfStar) {
                halfStarIcon()
            }
            repeat(emptyStars) {
                emptyStarIcon()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewStarRating() {
    Column {
        repeat(11) { rate ->
            PreviewNativeAdLayout(
                transformState = {
                    it.copy(starRating = rate / 2.0)
                }
            ) {
                StarRating()
            }
        }
    }
}
