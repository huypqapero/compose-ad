package com.apero.core.ads.ui.reward

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.apero.core.ads.data.AdsAdapter
import com.apero.core.ads.data.RewardAdsRepository
import com.apero.core.ads.ui.AdState
import com.apero.core.util.findActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.rememberKoinInject
import timber.log.Timber

@Composable
public fun rememberRewardAdLauncher(
    adState: AdState,
    permanentReward: Boolean,
    guardedRewardContentKey: Any? = null,
    reloadAfterShown: Boolean = false,
    rewardAdsRepository: RewardAdsRepository = rememberKoinInject(),
    adsAdapter: AdsAdapter = rememberKoinInject(),
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    activity: Activity = remember(context) {
        context.findActivity()
    },
    rewardState: MutableState<Boolean> = rememberSaveable(
        adState,
        permanentReward,
        guardedRewardContentKey,
        adState.enabled,
    ) {
        val rewarded = !adState.enabled
        mutableStateOf(rewarded)
    },
): RewardAdLauncher {
    val launcher = remember(
        adState,
        permanentReward,
        rewardState,
        reloadAfterShown,
        adsAdapter,
        rewardAdsRepository,
        activity,
        scope,
    ) {
        RewardAdLauncher(
            adState = adState,
            permanentReward = permanentReward,
            rewardState = rewardState,
            reloadAfterShown = reloadAfterShown,
            adsAdapter = adsAdapter,
            rewardAdsRepository = rewardAdsRepository,
            activity = activity,
            scope = scope
        )
    }
    return launcher
}

@Immutable
public class RewardAdLauncher(
    private val rewardState: MutableState<Boolean>,
    public val adState: AdState,
    private val reloadAfterShown: Boolean,
    private val permanentReward: Boolean,
    private val adsAdapter: AdsAdapter,
    private val rewardAdsRepository: RewardAdsRepository,
    private val activity: Activity,
    scope: CoroutineScope,
) {
    public val rewarded: RewardAdState = derivedStateOf {
        rewardState.value
    }

    init {
        scope.launch {
            with(activity) {
                rewardAdsRepository.loadRewardAdIfNecessary(adState.adId)
            }
        }
    }

    public suspend fun showAsync(): Boolean = with(activity) {
        if (!adState.enabled) {
            Timber.v("ad ${adState.adId}:showAsync skipped because disabled")
            return@with false
        }
        if (rewardState.value) {
            Timber.v("ad ${adState.adId}:showAsync skipped because rewardUiState is true")
            return@with false
        }

        val rewardAd = rewardAdsRepository.loadRewardAdIfNecessary(adState.adId)
        val adShown = rewardAd.isReady
        val rewardAdConsumed = adShown || rewardAd.isLoadFail

        if (adShown) {
            adsAdapter.showRewardAd(rewardAd)
            Timber.v("showAsync success")
        }

        if (rewardAdConsumed) {
            rewardAdsRepository.consumeRewardAd(adState.adId)
        }
        if (adShown && permanentReward) {
            rewardState.value = true
        }

        if (rewardAdConsumed && reloadAfterShown) {
            rewardAdsRepository.loadRewardAdIfNecessary(adState.adId)
        }

        adShown
    }
}
