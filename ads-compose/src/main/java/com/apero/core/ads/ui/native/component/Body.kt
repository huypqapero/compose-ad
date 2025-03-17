package com.apero.core.ads.ui.native.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.apero.core.ads.ui.native.NativeAdLayoutScope

@Composable
public fun NativeAdLayoutScope.Body(
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    style: TextStyle = LocalTextStyle.current,
) {
    NativeAdElementDelegate(
        onBindDelegate = {
            nativeAdView.bodyView = it
        }
    ) {
        ShimmerText(
            text = adUiState.body,
            style = style,
            maxLines = maxLines,
            modifier = modifier
        )
    }
}
