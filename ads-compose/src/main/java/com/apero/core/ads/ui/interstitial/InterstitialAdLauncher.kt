package com.apero.core.ads.ui.interstitial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.whenResumed
import com.apero.core.ads.data.AdsAdapter
import com.apero.core.ads.data.InterstitialAdsRepository
import com.apero.core.ads.ui.AdState
import com.apero.core.util.findActivity
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
public fun rememberInterstitialAdLauncher(
    adState: AdState,
    reloadAfterShown: Boolean,
    interAdsRepository: InterstitialAdsRepository = koinInject(),
    adsAdapter: AdsAdapter = koinInject(),
    context: Context = LocalContext.current,
    appCompatActivity: AppCompatActivity = remember(context) {
        context.findActivity() as AppCompatActivity
    },
): InterstitialAdLauncher {
    val launcher = remember(
        adState,
        reloadAfterShown,
        adsAdapter,
        interAdsRepository,
    ) {
        InterstitialAdLauncher(
            adState = adState,
            reloadAfterShown = reloadAfterShown,
            adsAdapter = adsAdapter,
            interstitialAdsRepository = interAdsRepository,
            appCompatActivity = appCompatActivity,
        )
    }
    LaunchedEffect(launcher) {
        Timber.v("preload")
        launcher.loadAsyncIfNecessary()
    }
    return launcher
}

@Immutable
public class InterstitialAdLauncher(
    private val adState: AdState,
    private val reloadAfterShown: Boolean,
    private val adsAdapter: AdsAdapter,
    private val interstitialAdsRepository: InterstitialAdsRepository,
    private val appCompatActivity: AppCompatActivity,
) {
    /**
     * Note: if you use CoroutineScope from rememberCoroutineScope there is a case when showAsync()
     * takes too long to complete and "CancelationException: The coroutine scope left the
     * composition" is thrown.
     * In such case. You must use LocalLifecycleOwner.current.lifecycleScope
     */
    public suspend fun showAsync(): Boolean = with(appCompatActivity) {
        if (!adState.enabled) {
            Timber.v("ad ${adState.debugName} showAsync skipped due to disabled")
            return@with false
        }
        val loadResult = interstitialAdsRepository.loadInterstitialAdIfNecessary(adState.adId)
        val adShown = loadResult
            .fold(
                ifLeft = { false },
                ifRight = {
                    Timber.v(it.toString())
                    if (it.isReady) {
                        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                            Timber.v("showInterstitialAd with id:${it.interstitialAd.adUnitId}")
                            adsAdapter.showInterstitialAd(it, false)
                                .isRight()
                        } else {
                            whenResumed {
                                Timber.v("showInterstitialAd with id:${it.interstitialAd.adUnitId}")
                                adsAdapter.showInterstitialAd(it, false)
                                    .isRight()
                            }
                        }
                    } else {
                        false
                    }
                },
            )

        val adConsumed = loadResult
            .fold(
                ifLeft = { true },
                ifRight = { it.isReady || it.isLoadFail }
            )

        if (adConsumed) {
            interstitialAdsRepository.consumeInterAd(adState.adId)
        }
        if (reloadAfterShown && adConsumed) {
            Timber.v("reload")
            interstitialAdsRepository.loadInterstitialAdIfNecessary(adState.adId)
        }

        adShown
    }

    public suspend fun loadAsyncIfNecessary(): Boolean {
        if (!adState.enabled) {
            Timber.v("ad ${adState.debugName} loadAsyncIfNecessary skipped due to disabled")
            return false
        }

        return interstitialAdsRepository.loadInterstitialAdIfNecessary(adState.adId)
            .isRight()
    }
}
