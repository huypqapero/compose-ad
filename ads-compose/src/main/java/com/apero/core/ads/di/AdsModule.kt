package com.apero.core.ads.di

import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AperoAd
import com.ads.control.billing.AppPurchase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.apero.core.ads")
public class AdsModule {
    @Single
    internal fun provideAppOpenManager(): AppOpenManager = AppOpenManager.getInstance()

    @Single
    internal fun provideAperoAd(): AperoAd = AperoAd.getInstance()

    @Single
    internal fun provideAdmob(): Admob = Admob.getInstance()

    @Single
    internal fun provideAppPurchase(): AppPurchase = AppPurchase.getInstance()
}
