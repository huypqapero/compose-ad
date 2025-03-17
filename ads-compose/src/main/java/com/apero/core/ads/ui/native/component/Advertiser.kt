package com.apero.core.ads.ui.native.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.apero.core.ads.ui.native.NativeAdLayoutScope

@Composable
public fun NativeAdLayoutScope.Advertiser(
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
) {
    if (adUiState.isReady && adUiState.advertiser == null) return

    NativeAdElementDelegate(
        onBindDelegate = {
            nativeAdView.advertiserView = it
        }
    ) {
        ShimmerText(
            text = adUiState.advertiser,
            style = style,
            maxLines = maxLines,
            modifier = modifier
        )
    }
}
