package com.apero.core.ads.ui.native.component

import androidx.compose.foundation.background
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.apero.core.ads.ui.native.NativeAdLayoutScope
import com.apero.core.ads.ui.native.isLoading
import com.valentinilk.shimmer.shimmer

@Composable
public fun NativeAdLayoutScope.ShimmerText(
    text: String?,
    modifier: Modifier = Modifier,
    loadingColor: Color = Color.Transparent,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    shape: Shape = RectangleShape,
    textAllCaps: Boolean = false,
) {
    val shimmerModifier = if (adUiState.isLoading) {
        Modifier
            .shimmer()
            .background(Color.LightGray)
    } else {
        Modifier
    }
    Text(
        text = text?.let { if (textAllCaps) it.uppercase() else it } ?: LOREM_IPSUM_SOURCE,
        style = if (adUiState.isLoading) style.copy(brush = null) else style,
        overflow = TextOverflow.Ellipsis,
        color = if (adUiState.isLoading) loadingColor else Color.Unspecified,
        maxLines = maxLines,
        modifier = Modifier
            .clip(shape)
            .then(shimmerModifier)
            .then(modifier)
    )
}

@Suppress("PrivatePropertyName")
private val LOREM_IPSUM_SOURCE = """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sodales
laoreet commodo. Phasellus a purus eu risus elementum consequat. Aenean eu
elit ut nunc convallis laoreet non ut libero. Suspendisse interdum placerat
risus vel ornare. Donec vehicula, turpis sed consectetur ullamcorper, ante
nunc egestas quam, ultricies adipiscing velit enim at nunc.
""".trimIndent()
