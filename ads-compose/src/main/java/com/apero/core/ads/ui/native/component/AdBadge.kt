package com.apero.core.ads.ui.native.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.apero.core.ads.R
import com.apero.core.ads.ui.native.NativeAdLayoutScope

@Composable
public fun NativeAdLayoutScope.AdBadge(
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    shape: Shape = RectangleShape,
) {
    ShimmerText(
        text = stringResource(R.string.title_badge_ad),
        style = style,
        shape = shape,
        loadingColor = style.color,
        modifier = modifier
    )
}
