package com.apero.core.ads.ext

import android.util.Log
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.core.view.children
import com.google.android.gms.ads.AdView

/**
 * Created by KO Huyn on 25/04/2024.
 */

public fun View.requestLayoutWithDelay(delayMillis: Long) {
    post {
        kotlin.runCatching {
            val t = parent.findAndroidComposeViewParent()
            t?.requestLayout() ?: this.postDelayed({
                val k = parent.findAndroidComposeViewParent()
                if (k != null) {
                    k.requestLayout()
                } else {
                    Log.i("TAG", "This should never happen")
                }
            }, delayMillis)
        }
    }
}

private fun ViewParent?.findAndroidComposeViewParent(): ViewParent? = when {
    this != null && this::class.java.simpleName == "AndroidComposeView" -> this
    this != null -> this.parent.findAndroidComposeViewParent()
    else -> null
}

public fun View.destroyBannerView() {
    findViewById<FrameLayout>(com.ads.control.R.id.banner_container)
        ?.let { bannerContainer ->
            val adView = bannerContainer.children.filterIsInstance<AdView>()
                .firstOrNull()
            if (adView != null) {
                adView.destroy()
                bannerContainer.removeView(adView)
            }
        }
}