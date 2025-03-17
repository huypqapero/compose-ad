package com.apero.core.ads.ui.native.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.apero.core.ads.ui.native.NativeAdLayoutScope

@Composable
public fun NativeAdLayoutScope.CallToAction(
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    shape: Shape = RectangleShape,
    textAllCaps: Boolean = false,
    onClick: () -> Unit = { /* no-op */ },
) {
    NativeAdElementDelegate(
        onBindDelegate = {
            nativeAdView.callToActionView = it
        },
        onClick = onClick,
    ) {
        ShimmerText(
            text = adUiState.cta,
            style = style,
            maxLines = maxLines,
            modifier = modifier,
            shape = shape,
            textAllCaps = textAllCaps,
        )
    }
}
