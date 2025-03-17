package com.apero.core.ads.data

import android.app.Activity
import android.content.Context
import androidx.annotation.LayoutRes
import androidx.collection.LruCache
import androidx.compose.runtime.Stable
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.event.AperoLogEventManager
import com.ads.control.funtion.AdType
import com.apero.core.ads.data.model.AdLifecycleEvent
import com.apero.core.ads.data.model.AdLifecycleEvent.ON_AD_CLICKED
import com.apero.core.ads.data.model.AdLifecycleEvent.ON_AD_CLOSED
import com.apero.core.ads.data.model.AdLifecycleEvent.ON_AD_FAILED_TO_LOAD
import com.apero.core.ads.data.model.AdLifecycleEvent.ON_AD_IMPRESSION
import com.apero.core.ads.data.model.AdLifecycleEvent.ON_AD_LOADED
import com.apero.core.ads.data.model.AdLifecycleEvent.ON_AD_OPENED
import com.apero.core.ads.data.model.AdLifecycleEvent.ON_AD_SWIPE_GESTURE_CLICKED
import com.apero.core.ads.data.model.AdUnitId
import com.apero.core.ads.data.model.AdUnitIdHighFloor
import com.apero.core.ads.ui.native.NativeAdWrapper
import com.apero.core.coroutine.di.CoreCoroutineModule
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import timber.log.Timber
import kotlin.coroutines.resume

public typealias NativeAdId = AdUnitId
public typealias NativeAdInstanceToken = String

internal data class NativeAdQuery(
    val adId: NativeAdId,
    val instanceToken: NativeAdInstanceToken,
    @LayoutRes
    val layoutId: Int?,
)

@Single
@Stable
public class NativeAdsRepository(
    private val context: Context,
    private val adsAdapter: AdsAdapter,
    private val appScope: CoroutineScope,
    @Named(CoreCoroutineModule.MAIN)
    private val mainDispatcher: CoroutineDispatcher,
    @Named(CoreCoroutineModule.DEFAULT)
    private val defaultDispatcher: CoroutineDispatcher,
) {

    private val cachedApNativeAds =
        LruCache<NativeAdQuery, Deferred<Either<ApAdError, ApNativeAd>>>(LRU_SIZE)

    private val cachedNativeAds =
        LruCache<NativeAdQuery, Deferred<Either<LoadAdError, NativeAdWrapper>>>(LRU_SIZE)

    context(Activity)
    @Suppress("DeferredResultUnused")
    internal suspend fun loadNativeAdIfNecessary(
        id: NativeAdId,
        token: NativeAdInstanceToken,
        @LayoutRes
        layoutId: Int,
    ): Either<ApAdError, ApNativeAd> {
        val queryKey = NativeAdQuery(id, token, layoutId)
        val current = cachedApNativeAds.get(queryKey)
        val shouldLoad = current == null || (current.isCompleted && current.getCompleted()
            .fold(ifLeft = { true }, ifRight = { it.isLoadFail }))

        if (shouldLoad) {
            Timber.v("load new $queryKey")
        } else {
            Timber.v("re-use $queryKey")
        }

        return if (shouldLoad) {
            val deferred = appScope.async(mainDispatcher) {
                if (id is AdUnitIdHighFloor) {
                    val adHighFloor = adsAdapter.loadNative(id.adUnitIdHighFloor, layoutId)
                    if (adHighFloor.isLeft()) {
                        adsAdapter.loadNative(id.adUnitIdAllPrice, layoutId)
                    } else adHighFloor
                } else {
                    adsAdapter.loadNative(id.adUnitId, layoutId)
                }
            }
            cachedApNativeAds.put(queryKey, deferred)
            deferred.await()
        } else {
            current!!.await()
        }
    }

    @Suppress("DeferredResultUnused")
    public suspend fun loadNativeAdIfNecessary(
        id: NativeAdId,
        token: NativeAdInstanceToken,
    ): Either<LoadAdError, NativeAdWrapper> {
        val queryKey = NativeAdQuery(id, token, null)

        val current = cachedNativeAds[queryKey]
        val shouldLoad = current == null || (
            current.isCompleted &&
            current.getCompleted().isLeft())

        if (shouldLoad) {
            Timber.v("load new $queryKey")
        } else {
            Timber.v("re-use $queryKey")
        }

        return if (shouldLoad) {
            val deferred = appScope.async(defaultDispatcher) {
                if (id is AdUnitIdHighFloor) {
                    context.loadNativeAd(id.adUnitIdHighFloor).let {
                        if (it.isLeft()) context.loadNativeAd(id.adUnitIdAllPrice)
                        else it
                    }
                } else {
                    context.loadNativeAd(id.adUnitId)
                }
            }
            cachedNativeAds.put(queryKey, deferred)
            deferred.await()
        } else {
            current!!.await()
        }
    }

    @Deprecated("no usage")
    public fun consumeCache(
        id: NativeAdId,
        token: NativeAdInstanceToken,
    ): Boolean {
        return cachedNativeAds.remove(NativeAdQuery(id, token, null)) != null
    }

    public fun getFromCache(
        id: NativeAdId,
        token: NativeAdInstanceToken,
    ): Either<LoadAdError, NativeAdWrapper>? {
        return try {
            cachedNativeAds[NativeAdQuery(id, token, null)]
                ?.getCompleted()
        } catch (e: IllegalStateException) {
            null
        }
    }

    internal companion object {
        const val LRU_SIZE = 50
    }
}

private suspend fun Context.loadNativeAd(
    nativeAdId: String,
    nativeAdOptions: NativeAdOptions = NativeAdOptions.Builder()
        .setVideoOptions(
            VideoOptions.Builder()
                .setStartMuted(true)
                .build()
        )
        .build(),
    adRequest: AdRequest = AdRequest.Builder()
        .build(),
): Either<LoadAdError, NativeAdWrapper> = suspendCancellableCoroutine { continuation ->
    val lifecycleFlow = MutableSharedFlow<AdLifecycleEvent>(replay = 1)
    val adLoader = AdLoader.Builder(this, nativeAdId)
        .forNativeAd { ad: NativeAd ->
            continuation.resume(NativeAdWrapper(ad, nativeAdId, lifecycleFlow.asSharedFlow()).right())
            ad.setOnPaidEventListener { adValue ->
                AperoLogEventManager.logPaidAdImpression(
                    this,
                    adValue,
                    nativeAdId,
                    ad.responseInfo,
                    AdType.NATIVE
                )
            }
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                continuation.resume(adError.left())
                lifecycleFlow.tryEmit(ON_AD_FAILED_TO_LOAD)
            }

            override fun onAdClicked() {
                AperoLogEventManager.logClickAdsEvent(this@loadNativeAd, nativeAdId)
                lifecycleFlow.tryEmit(ON_AD_CLICKED)
            }

            override fun onAdImpression() {
                lifecycleFlow.tryEmit(ON_AD_IMPRESSION)
            }

            override fun onAdLoaded() {
                lifecycleFlow.tryEmit(ON_AD_LOADED)
            }

            override fun onAdClosed() {
                lifecycleFlow.tryEmit(ON_AD_CLOSED)
            }

            override fun onAdOpened() {
                lifecycleFlow.tryEmit(ON_AD_OPENED)
            }

            override fun onAdSwipeGestureClicked() {
                lifecycleFlow.tryEmit(ON_AD_SWIPE_GESTURE_CLICKED)
            }
        })
        .withNativeAdOptions(nativeAdOptions)
        .build()

    adLoader.loadAd(adRequest)
}
