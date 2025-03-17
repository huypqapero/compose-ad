package com.apero.core.ads.ui.native

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import arrow.core.Either
import com.apero.core.ads.R
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

private const val LOADING_MILLIS = 2_500L

@Composable
public fun PreviewNativeAdLayout(
    modifier: Modifier = Modifier,
    loadingMillis: Long = LOADING_MILLIS,
    transformState: (NativeAdUiState) -> NativeAdUiState = { it },
    content: @Composable NativeAdLayoutScope.(NativeAdUiState) -> Unit,
) {
    val loadingState = rememberInspectionAdState(loadingMillis)
    val context = LocalContext.current

    val adUiState by remember {
        derivedStateOf { transformState(loadingState.value.value) }
    }
    val nativeAdLayoutScope = remember(adUiState) {
        NativeAdLayoutScopeImpl(NativeAdView(context), adUiState)
    }
    Box(
        propagateMinConstraints = true,
        modifier = modifier
    ) {
        content(nativeAdLayoutScope, adUiState)
    }
}

@Composable
internal fun rememberInspectionAdState(
    loadingMillis: Long = LOADING_MILLIS,
    context: Context = LocalContext.current,
    finalState: NativeAdUiState = remember {
        NativeAdUiState(
            ad = null,
            headline = "Test Ad : Google Ads",
            body = "Stay up to date with your Ads Check how your ads are performing",
            cta = "INSTALL",
            price = "FREE",
            store = "Google Play",
            starRating = 4.5,
            icon = NativeAdUiState.Image(
                scale = 1.0,
                uri = "https://lh3.googleusercontent.com/zAZyDZl4s0daHzIOd5oy4xQb6eqYiG1iWh5RBpKD3xwNGBLmf4kg3s4iRwLsYkW3d0xrXZvktQ=w128-h128-n-e7-rw-v1".toUri(),
                drawable = AppCompatResources.getDrawable(
                    context,
                    R.drawable.preview_native_ad_icon
                ),
            ),
            advertiser = null,
            images = persistentListOf(
                NativeAdUiState.Image(
                    scale = 1.0,
                    uri = "https://lh3.googleusercontent.com/nGVGy0QmnOCN28MXA97L6QY8FUEOsJua1pBf7P-GXQWn5nCCcyGNEb0t-7RbfveuUsI-JN_aZzM=w945-h512-n-e7-rw-v1".toUri(),
                    drawable = AppCompatResources.getDrawable(
                        context,
                        R.drawable.preview_native_ad_image
                    ),
                ),
            ),
            isReady = true,
        )
    },
): State<Either.Right<NativeAdUiState>> {
    val adUiState by produceState(
        if (loadingMillis == 0L) finalState else NativeAdUiState(null)
    ) {
        delay(loadingMillis)
        value = finalState
    }
    return remember(adUiState) {
        derivedStateOf {
            Either.Right(adUiState)
        }
    }
}
