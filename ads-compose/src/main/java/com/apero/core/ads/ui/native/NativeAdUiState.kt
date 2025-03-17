package com.apero.core.ads.ui.native

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Immutable
import com.apero.core.ads.data.model.AdLifecycleEvent
import com.google.ads.mediation.adcolony.AdColonyMediationAdapter
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.ads.mediation.applovin.AppLovinMediationAdapter
import com.google.ads.mediation.facebook.FacebookMediationAdapter
import com.google.ads.mediation.mintegral.MintegralMediationAdapter
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.google.ads.mediation.vungle.VungleMediationAdapter
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

public data class NativeAdWrapper internal constructor(
    internal val value: NativeAd,
    val adUnitId: String,
    val lifecycleEventFlow: SharedFlow<AdLifecycleEvent>,
)

@Immutable
public data class NativeAdUiState(
    public val ad: NativeAdWrapper?,
    public val headline: String? = ad?.value?.headline,
    public val body: String? = ad?.value?.body,
    public val cta: String? = ad?.value?.callToAction,
    public val price: String? = ad?.value?.price,
    public val store: String? = ad?.value?.store,
    public val starRating: Double? = ad?.value?.starRating,
    public val icon: Image? = ad?.value?.icon?.let(NativeAdUiState::Image),
    public val advertiser: String? = ad?.value?.advertiser,
    public val images: ImmutableList<Image> = ad?.value?.images
        .orEmpty()
        .map(NativeAdUiState::Image)
        .toImmutableList(),
    public val isReady: Boolean = ad != null,
    public val mediation: AdNativeMediation? = ad?.value?.let { AdNativeMediation.get(it) }
) {

    public data class Image(val scale: Double, val uri: Uri?, val drawable: Drawable?) {
        internal constructor(image: NativeAd.Image) : this(image.scale, image.uri, image.drawable)
    }
}

internal val NativeAdUiState.isLoading: Boolean get() = !isReady

public enum class AdNativeMediation(public val clazz: Class<*>) {
    ADMOB(AdMobAdapter::class.java),
    FACEBOOK(FacebookMediationAdapter::class.java),
    ADCOLONY(AdColonyMediationAdapter::class.java),
    APPLOVIN(AppLovinMediationAdapter::class.java),
    MINTEGRAL(MintegralMediationAdapter::class.java),
    PANGLE(PangleMediationAdapter::class.java),
    VUNGLE(VungleMediationAdapter::class.java);

    public companion object {

        public fun get(nativeAd: NativeAd): AdNativeMediation? {
            val adapterClassName = nativeAd.responseInfo?.mediationAdapterClassName ?: return null
            return entries.find { adapterClassName.contains(it.clazz.simpleName) }
        }
    }
}
