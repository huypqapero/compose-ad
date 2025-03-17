package com.apero.core.ads.data.model

import android.os.Parcelable
import androidx.annotation.LayoutRes
import com.ads.control.R
import com.ads.control.helper.adnative.NativeAdConfig
import com.ads.control.helper.adnative.highfloor.NativeAdHighFloorConfig
import kotlinx.parcelize.Parcelize

/**
 * Created by KO Huyn on 06/05/2024.
 */

public abstract class AdUnitId : Parcelable {
    public abstract val adUnitName: String
    public abstract val adUnitId: String
    public abstract val isEnableShowAd: Boolean
    public fun toNativeAdConfig(
        canReloadAd: Boolean,
        @LayoutRes layoutIdNative: Int
    ): NativeAdConfig {
        return when (this) {
            is AdUnitIdSingle -> {
                NativeAdConfig(
                    adUnitId,
                    isEnableShowAd,
                    canReloadAd,
                    layoutIdNative
                )
            }

            is AdUnitIdHighFloor -> {
                NativeAdHighFloorConfig(
                    adUnitIdHighFloor,
                    adUnitIdAllPrice,
                    isEnableShowAd,
                    canReloadAd,
                    layoutIdNative
                )
            }

            else -> throw UnsupportedOperationException()
        }
    }

    public companion object {
        public fun create(
            adUnitName: String,
            adUnitId: String,
            isEnableShowAd: Boolean,
            adUnitIdHighFloor: String,
            adUnitIdAllPrice: String,
            isUsingHighFloor: Boolean,
        ): AdUnitId {
            return if (isUsingHighFloor) {
                AdUnitIdHighFloor(
                    adUnitName = adUnitName,
                    adUnitIdHighFloor = adUnitIdHighFloor,
                    adUnitIdAllPrice = adUnitIdAllPrice,
                    isEnableShowAd = isEnableShowAd
                )
            } else {
                AdUnitIdSingle(
                    adUnitId = adUnitId,
                    adUnitName = adUnitName,
                    isEnableShowAd = isEnableShowAd
                )
            }
        }

        public fun create(
            adUnitName: String,
            adUnitId: String,
            isEnableShowAd: Boolean,
            adUnitIdHighFloor: String,
            isUsingHighFloor: Boolean,
        ): AdUnitId {
            return if (isUsingHighFloor) {
                AdUnitIdHighFloor(
                    adUnitName = adUnitName,
                    adUnitIdHighFloor = adUnitIdHighFloor,
                    adUnitIdAllPrice = adUnitId,
                    isEnableShowAd = isEnableShowAd
                )
            } else {
                AdUnitIdSingle(
                    adUnitId = adUnitId,
                    adUnitName = adUnitName,
                    isEnableShowAd = isEnableShowAd
                )
            }
        }

        public fun create(
            adUnitId: String, adUnitName: String, isEnableShowAd: Boolean
        ): AdUnitId {
            return AdUnitIdSingle(
                adUnitId = adUnitId,
                adUnitName = adUnitName,
                isEnableShowAd = isEnableShowAd
            )
        }
    }
}

@Parcelize
public data class AdUnitIdSingle(
    public override val adUnitName: String,
    public override val adUnitId: String,
    public override val isEnableShowAd: Boolean
) : AdUnitId()

@Parcelize
public data class AdUnitIdHighFloor(
    public override val adUnitName: String,
    public val adUnitIdHighFloor: String,
    public val adUnitIdAllPrice: String,
    override val adUnitId: String = adUnitIdAllPrice,
    public override val isEnableShowAd: Boolean
) : AdUnitId()