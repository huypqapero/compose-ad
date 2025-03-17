package com.apero.core.ads.data

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Stable
import androidx.core.view.isGone
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AperoAd
import com.ads.control.ads.AperoAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.ads.wrapper.ApRewardAd
import com.ads.control.ads.wrapper.ApRewardItem
import com.ads.control.billing.AppPurchase
import com.ads.control.funtion.AdCallback
import com.apero.core.ads.data.model.AdUnitId
import com.apero.core.ads.ext.AdInspection
import com.apero.core.ads.ext.requestLayoutWithDelay
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Single
@Stable
public class AdsAdapter internal constructor(
    private val aperoAd: AperoAd,
    private val appPurchase: AppPurchase,
    private val appOpenManager: AppOpenManager,
    private val context: Context,
) {

    context(Context)
    public suspend fun loadSplashInterAd(
        adId: String,
    ): Either<ApAdError?, Unit> = suspendCoroutine { continuation ->
        aperoAd.loadSplashInterstitialAds(
            this@Context,
            adId,
            InterstitialAdsRepository.TIMEOUT_SPLASH_AD,
            InterstitialAdsRepository.TIME_DELAY,
            false,
            object : AperoAdCallback() {
                override fun onAdSplashReady() {
                    try {
                        continuation.resume(Unit.right())
                        Timber.d("onAdSplashReady: ")
                    } catch (e: IllegalStateException) {
                        Timber.w(e, "onAdSplashReady: ")
                    }
                }

                override fun onNextAction() {
                    try {
                        continuation.resume(null.left())
                        Timber.d("onNextAction: ")
                    } catch (e: Exception) {
                        Timber.w(e, "onNextAction: ")
                    }
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    try {
                        continuation.resume(adError!!.left())
                        Timber.d("onAdFailedToLoad: adError " + adError.message)
                    } catch (e: Exception) {
                        Timber.w(e, "onAdFailedToLoad: ")
                    }
                }
            }
        )
    }

    context(Context)
    public suspend fun loadSplashAppOpenAd(
        adId: String,
    ): Either<ApAdError?, Unit> = suspendCoroutine { continuation ->
        appOpenManager.setSplashAdId(adId)
        aperoAd.loadAppOpenSplashSameTime(
            this@Context,
            "",
            InterstitialAdsRepository.TIMEOUT_SPLASH_AD,
            InterstitialAdsRepository.TIME_DELAY,
            false,
            object : AperoAdCallback() {
                override fun onAdSplashReady() {
                    try {
                        continuation.resume(Unit.right())
                        Timber.d("onAdSplashReady: ")
                    } catch (e: IllegalStateException) {
                        Timber.w(e, "onAdSplashReady: ")
                    }
                }

                override fun onNextAction() {
                    try {
                        continuation.resume(null.left())
                        Timber.d("onNextAction: ")
                    } catch (e: Exception) {
                        Timber.w(e, "onNextAction: ")
                    }
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    try {
                        continuation.resume(adError!!.left())
                        Timber.d("onAdFailedToLoad: adError " + adError.message)
                    } catch (e: Exception) {
                        Timber.w(e, "onAdFailedToLoad: ")
                    }
                }
            }

        )
    }

    context(Context)
    public suspend fun showSplashAppOpenAd(
    ): Either<AdError?, Unit> = suspendCoroutine { continuation ->
        appOpenManager.showAppOpenSplash(
            this@Context,
            object : AdCallback() {
                override fun onNextAction() {
                    try {
                        continuation.resume(Unit.right())
                    } catch (e: Exception) {
                        Timber.e(e, "showSplashAppOpenAd: ")
                    }
                }

                override fun onAdFailedToShow(adError: AdError?) {
                    continuation.resume(adError!!.left())
                }
            },
        )
    }
        .onLeft {
            Timber.w("showSplashAppOpenAd error: ${it.message}")
        }

    context(Activity)
    public suspend fun loadNative(
        id: String,
        @LayoutRes
        layoutId: Int,
    ): Either<ApAdError, ApNativeAd> = suspendCoroutine { continuation ->
        aperoAd.loadNativeAdResultCallback(
            this@Activity,
            id,
            layoutId,
            object : AperoAdCallback() {
                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    continuation.resume(nativeAd.right())
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    continuation.resume(adError!!.left())
                }

                override fun onAdImpression() {
                    Timber.v("onAdImpression")
                }
            }
        )
    }
        .onLeft {
            Timber.w("loadNative error: ${it.message}")
        }

    context(AppCompatActivity)
    public suspend fun showSplashInterstitialAd(
    ): Either<ApAdError?, Unit> = suspendCoroutine { continuation ->
        aperoAd.onShowSplash(
            this@AppCompatActivity,
            object : AperoAdCallback() {
                override fun onNextAction() {
                    try {
                        continuation.resume(Unit.right())
                    } catch (e: Exception) {
                        Timber.e(e, "showSplashInterstitialAd: ")
                    }
                }

                override fun onAdFailedToShow(adError: ApAdError?) {
                    continuation.resume(adError!!.left())
                }
            }
        )
    }
        .onLeft {
            Timber.w("showSplashInterstitialAd error: ${it.message}")
        }

    context(AppCompatActivity)
    public suspend fun loadBanner(
        rootView: View,
        adId: AdUnitId,
    ): Boolean = suspendCoroutine { continuation ->
        Timber.v("loadBanner()")
        if (appPurchase.isPurchased) {
            rootView.isGone = true
        } else {
            aperoAd.loadBannerFragment(
                this@AppCompatActivity,
                adId.adUnitId,
                rootView,
                object : AdCallback() {
                    override fun onAdFailedToLoad(i: LoadAdError?) {
                        Timber.d("onAdFailedToLoad() called with: i = $i")
                        continuation.resume(false)
                    }

                    override fun onAdLoaded() {
                        rootView.requestLayoutWithDelay(400)
                        continuation.resume(true)
                        Timber.d("onAdLoaded() called")
                    }

                    override fun onAdImpression() {
                        Timber.d("onAdImpression() called")
                    }
                })
        }
    }



    context(AppCompatActivity)
    public suspend fun loadBannerCollapse(
        rootView: View,
        gravity: String,
        adId: AdUnitId,
    ): Boolean = suspendCoroutine { continuation ->
        Timber.v("loadBannerCollapse()")
        if (appPurchase.isPurchased) {
            rootView.isGone = true
        } else {
            aperoAd.loadCollapsibleBannerFragment(
                this@AppCompatActivity,
                adId.adUnitId,
                rootView,
                gravity,
                object : AdCallback() {
                    override fun onAdFailedToLoad(i: LoadAdError?) {
                        Timber.d("onAdFailedToLoad() called with: i = $i")
                        continuation.resume(false)
                    }

                    override fun onAdLoaded() {
                        rootView.requestLayoutWithDelay(400)
                        continuation.resume(true)
                        Timber.d("onAdLoaded() called")
                    }

                    override fun onAdImpression() {
                        Timber.d("onAdImpression() called")
                    }
                })
        }
    }

    context(AppCompatActivity)
    public fun populateNativeAd(
        apNativeAd: ApNativeAd,
        adPlaceHolder: FrameLayout,
        containerShimmerLoading: ShimmerFrameLayout,
    ) {
        aperoAd.populateNativeAdView(
            this@AppCompatActivity,
            apNativeAd,
            adPlaceHolder,
            containerShimmerLoading,
        )
    }
    context(AppCompatActivity)
    public suspend fun showInterstitialAd(
        interstitialAd: ApInterstitialAd,
        shouldReload: Boolean = false,
    ): Either<Unit, Unit> = suspendCoroutine { continuation ->
        aperoAd.forceShowInterstitial(
            this@AppCompatActivity,
            interstitialAd,
            object : AperoAdCallback() {
                override fun onAdImpression() {
                    super.onAdImpression()
                    if (AdInspection.isEnableMessageForTester) {
                        Toast.makeText(context, interstitialAd.interstitialAd.adUnitId, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onNextAction() {
                    Timber.v("showInterstitialAd\$onNextAction")
                    try {
                        continuation.resume(Unit.right())
                    } catch (e: IllegalStateException) {
                        Timber.e(e, "onNextAction")
                    }
                }

                override fun onAdFailedToShow(adError: ApAdError?) {
                    Timber.v("showInterstitialAd\$onAdFailedToShow ${adError?.message}")
                    try {
                        continuation.resume(Unit.left())
                    } catch (e: IllegalStateException) {
                        Timber.e(e, "onAdFailedToShow")
                    }
                }
            },
            shouldReload,
        )
    }

    public suspend fun loadInterstitialAd(
        id: String,
    ): Either<ApAdError, ApInterstitialAd> = suspendCoroutine { continuation ->
        aperoAd.getInterstitialAds(
            context,
            id,
            object : AperoAdCallback() {
                override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                    continuation.resume(interstitialAd!!.right())
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    continuation.resume(adError!!.left())
                }
            }
        )
    }
        .onLeft {
            Timber.w("loadInterstitialAd error: ${it.message}")
        }

    context(Activity)
    public suspend fun loadRewardAd(
        id: RewardAdId,
    ): ApRewardAd = coroutineScope {
        aperoAd.getRewardAd(this@Activity, id.adUnitId)
    }

    context(Activity)
    public suspend fun showRewardAd(
        apRewardAd: ApRewardAd,
    ): Either<ApAdError, Boolean> = suspendCoroutine { continuation ->
        var isUserEarnReward = false
        aperoAd.forceShowRewardAd(
            this@Activity,
            apRewardAd,
            object : AperoAdCallback() {
                override fun onNextAction() {
                    continuation.resume(isUserEarnReward.right())
                }

                override fun onUserEarnedReward(rewardItem: ApRewardItem) {
                    isUserEarnReward = true
                }

                override fun onAdFailedToShow(adError: ApAdError?) {
                    continuation.resume(adError!!.left())
                }
            }
        )
    }
}
