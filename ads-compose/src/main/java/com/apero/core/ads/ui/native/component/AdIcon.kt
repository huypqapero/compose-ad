package com.apero.core.ads.ui.native.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.apero.core.ads.ui.native.NativeAdLayoutScope
import com.valentinilk.shimmer.shimmer

@Composable
public fun NativeAdLayoutScope.AdIcon(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
) {
    if (adUiState.isReady && adUiState.icon == null) return

    NativeAdElementDelegate(
        onBindDelegate = {
            nativeAdView.iconView = it
        }
    ) {
        val shimmerModifier = if (adUiState.icon == null) {
            Modifier
                .shimmer()
                .background(Color.LightGray)
        } else {
            Modifier
        }

        adUiState.icon?.drawable?.let { drawable ->
            Box(
                modifier = Modifier
                    .clip(shape)
                    .then(shimmerModifier)
                    .then(modifier)
                    .drawWithContent {
                        drawIntoCanvas {
                            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                            drawable.draw(it.nativeCanvas)
                        }
                    }
            )
        } ?: run {
            adUiState.icon?.uri?.let { uri ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .build(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(shape),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}
