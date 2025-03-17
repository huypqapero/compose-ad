package com.apero.core.ads.ui.native

import android.view.View
import android.view.ViewParent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.get
import arrow.core.Either
import arrow.core.right
import com.apero.core.ads.data.NativeAdInstanceToken
import com.apero.core.ads.data.NativeAdsRepository
import com.apero.core.ads.ui.AdState
import com.google.android.gms.ads.LoadAdError
import org.koin.compose.koinInject
import timber.log.Timber
import java.util.UUID

@Composable
public fun NativeAdLayout(
    adState: AdState,
    modifier: Modifier = Modifier,
    shouldRequestLayoutNativeAd: Boolean,
    onFailAd: () -> Unit = {},
    instanceToken: NativeAdInstanceToken = rememberSaveable {
        UUID.randomUUID().toString()
    },
    loadResultState: State<Either<LoadAdError, NativeAdUiState>> = if (LocalInspectionMode.current) {
        rememberInspectionAdState()
    } else {
        rememberLoadNativeAd(instanceToken, adState)
    },
    content: @Composable NativeAdLayoutScope.(NativeAdUiState) -> Unit,
) {
    if (LocalInspectionMode.current) {
        PreviewNativeAdLayout(modifier = modifier, loadingMillis = 0L, content = content)
    } else {
        NativeAdLayoutImpl(
            adState = adState,
            modifier = modifier,
            instanceToken = instanceToken,
            loadResultState = loadResultState,
            shouldRequestLayoutNativeAd = shouldRequestLayoutNativeAd,
            onAdsShown = { /* no-op */ },
            onFailAd = onFailAd,
            content = content,
        )
    }
}

@Composable
internal fun NativeAdLayoutImpl(
    adState: AdState,
    modifier: Modifier = Modifier,
    shouldRequestLayoutNativeAd: Boolean,
    onAdsShown: () -> Unit,
    onFailAd: () -> Unit = {},
    instanceToken: NativeAdInstanceToken,
    loadResultState: State<Either<LoadAdError, NativeAdUiState>>,
    content: @Composable NativeAdLayoutScope.(NativeAdUiState) -> Unit,
) {
    val loadResult by loadResultState

    val adUiState = loadResult.getOrNull()

    LaunchedEffect(loadResult) {
        if (loadResult.isLeft()) {
            onFailAd()
        }
    }

    if (adState.enabled && adUiState != null) {
        AndroidView(
            factory = {
                com.google.android.gms.ads.nativead.NativeAdView(it)
                    .apply {
                        addView(ComposeView(it))
//                        if (shouldRequestLayoutNativeAd) {
//                            requestLayoutWithDelay(400)
//                        }
                    }
            },
            update = { view ->
                val composeView = view[0] as ComposeView
                val nativeAdLayoutScope = NativeAdLayoutScopeImpl(
                    view, adUiState
                )
                Timber.i("${adState.adId}, $instanceToken onUpdate")

                composeView.setContent {
                    nativeAdLayoutScope.content(adUiState)
                }

                adUiState.ad?.value?.let { ad ->
                    view.setNativeAd(ad)
                    onAdsShown()
                }
                if (shouldRequestLayoutNativeAd) {
                    view.requestLayoutWithDelay(400)
                }
            },
            onReset = {
                /* no-op */
            },
            onRelease = {
                it.destroy()
            },
            modifier = Modifier
                .then(modifier)
        )
    }
}
private fun View.requestLayoutWithDelay(delayMillis: Long) {
    post {
        kotlin.runCatching {
            val t = parent.findAndroidComposeViewParent()
            t?.requestLayout() ?: this.postDelayed({
                parent.findAndroidComposeViewParent()?.requestLayout()
            }, delayMillis)
        }
    }
}

private fun ViewParent?.findAndroidComposeViewParent(): ViewParent? = when {
    this != null && this::class.java.simpleName == "AndroidComposeView" -> this
    this != null -> this.parent.findAndroidComposeViewParent()
    else -> null
}
@Composable
public fun rememberLoadNativeAd(
    instanceToken: NativeAdInstanceToken,
    adState: AdState,
    nativeAdsRepository: NativeAdsRepository = koinInject(),
): State<Either<LoadAdError, NativeAdUiState>> =
    produceState(
        initialValue = nativeAdsRepository
            .getFromCache(adState.adId, instanceToken)
            ?.map(::NativeAdUiState)
            ?: NativeAdUiState(null).right(),
        instanceToken,
        adState.adId,
        adState.enabled,
    ) {
        if (!adState.enabled) return@produceState

        value = nativeAdsRepository
            .loadNativeAdIfNecessary(
                adState.adId,
                instanceToken,
            )
            .map(::NativeAdUiState)
            .onLeft {
                Timber.w("loadNative error: ${it.message}")
            }
            .onRight {
                Timber.w("loadNative success. debugName:${adState.debugName}, token:$instanceToken $it")
            }
    }
