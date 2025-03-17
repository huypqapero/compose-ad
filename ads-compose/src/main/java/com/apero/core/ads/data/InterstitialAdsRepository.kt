package com.apero.core.ads.data

import androidx.compose.runtime.Stable
import arrow.core.Either
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.apero.core.ads.data.model.AdUnitId
import com.apero.core.ads.data.model.AdUnitIdHighFloor
import com.apero.core.coroutine.di.CoreCoroutineModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

public typealias InterstitialAdId = AdUnitId

@Single
@Stable
public class InterstitialAdsRepository internal constructor(
    private val adsAdapter: AdsAdapter,
    private val appScope: CoroutineScope,
    @Named(CoreCoroutineModule.MAIN)
    private val mainDispatcher: CoroutineDispatcher,
) {
    public companion object {
        public const val TIMEOUT_SPLASH_AD: Long = 30000L
        public const val TIME_DELAY: Long = 5000L
    }

    private val cachedInterAds: MutableMap<InterstitialAdId, Deferred<Either<ApAdError, ApInterstitialAd>>> =
        ConcurrentHashMap(10)

    public fun consumeInterAd(id: InterstitialAdId) {
        @Suppress("DeferredResultUnused")
        cachedInterAds.remove(id)
    }

    private val mutex = Mutex()

    public suspend fun loadInterstitialAdIfNecessary(
        id: InterstitialAdId,
    ): Either<ApAdError, ApInterstitialAd> {
        Timber.v("loadInterstitialAdIfNecessary()")
        val current = mutex.withLock {
            cachedInterAds[id]
        }
        val shouldLoad = current == null ||
            current.isCompleted &&
            current.getCompleted().fold(
                ifLeft = { true },
                ifRight = { it.isLoadFail },
            )

        return if (shouldLoad) {
            val deferred = mutex.withLock {
                val deferred = appScope.async(mainDispatcher) {
                    if (id is AdUnitIdHighFloor) {
                        val highFloor = adsAdapter.loadInterstitialAd(id.adUnitIdHighFloor)
                        if (highFloor.isLeft()) {
                            adsAdapter.loadInterstitialAd(id.adUnitIdAllPrice)
                        } else {
                            highFloor
                        }
                    } else {
                        adsAdapter.loadInterstitialAd(id.adUnitId)
                    }
                }
                cachedInterAds[id] = deferred
                deferred
            }
            deferred.await()
        } else {
            current!!.await()
        }
    }
}
