package com.apero.core.ads.ui.native

import androidx.compose.runtime.Immutable
import com.google.android.gms.ads.nativead.NativeAdView

public abstract class NativeAdLayoutScope {
    internal abstract val nativeAdView: NativeAdView
    internal abstract val adUiState: NativeAdUiState
}

@Immutable
internal class NativeAdLayoutScopeImpl(
    override val nativeAdView: NativeAdView,
    override val adUiState: NativeAdUiState,
) : NativeAdLayoutScope()
