package com.apero.core.ads.ui.native.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apero.core.ads.ui.native.NativeAdLayoutScope
import com.apero.core.ads.ui.native.isLoading
import com.google.android.gms.ads.nativead.MediaView
import com.valentinilk.shimmer.shimmer

@Composable
public fun NativeAdLayoutScope.MediaView(modifier: Modifier = Modifier) {
    val newModifier = Modifier
        .requiredSizeIn(minWidth = 120.dp, minHeight = 120.dp)
        .then(modifier)

    if (adUiState.isLoading) {
        Box(
            modifier = Modifier
                .shimmer()
                .background(Color.LightGray)
                .then(newModifier)
        )
    } else {
        AndroidView(
            factory = { context ->
                MediaView(context).also {
                    nativeAdView.mediaView = it
                }
            },
            update = {
                nativeAdView.mediaView = it
                adUiState.ad?.value?.let(nativeAdView::setNativeAd)
            },
            onRelease = {
                nativeAdView.mediaView = null
            },
            onReset = {
                /* no-op */
            },
            modifier = newModifier
        )
    }
}
