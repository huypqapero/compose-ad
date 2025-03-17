package com.apero.core.ads.data

import android.app.Activity
import com.ads.control.ads.wrapper.ApRewardAd
import com.apero.core.ads.data.model.AdUnitId
import com.apero.core.coroutine.di.CoreCoroutineModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.concurrent.ConcurrentHashMap

public typealias RewardAdId = AdUnitId

@Single
public class RewardAdsRepository(
    private val adsAdapter: AdsAdapter,
    private val appScope: CoroutineScope,
    @Named(CoreCoroutineModule.MAIN)
    private val mainDispatcher: CoroutineDispatcher,
) {

    private val cachedRewardAds: MutableMap<RewardAdId, Deferred<ApRewardAd>> =
        ConcurrentHashMap(10)

    public fun consumeRewardAd(id: RewardAdId) {
        @Suppress("DeferredResultUnused")
        cachedRewardAds.remove(id)
    }

    context(Activity)
    public suspend fun loadRewardAdIfNecessary(
        id: RewardAdId,
    ): ApRewardAd {
        val current = cachedRewardAds[id]
        val shouldLoad = current == null ||
            current.isCompleted && current.getCompleted().isLoadFail

        return if (shouldLoad) {
            val deferred = appScope.async(mainDispatcher) {
                adsAdapter.loadRewardAd(id)
            }
            cachedRewardAds[id] = deferred
            deferred.await()
        } else {
            current!!.await()
        }
    }
}
