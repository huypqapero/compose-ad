package com.apero.core.ads.ui.banner

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ads.control.util.AppConstant.CollapsibleGravity
import com.apero.core.ads.R
import com.apero.core.ads.data.AdsAdapter
import com.apero.core.ads.ext.destroyBannerView
import com.apero.core.ads.ui.AdState
import com.apero.core.util.findActivity
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

public typealias BannerAdInstanceToken = String

@Composable
public fun PreviewBannerAdView(
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    Box(modifier = modifier
        .height(56.dp)
        .fillMaxWidth()
        .drawWithContent {
            drawContent()
            drawRect(Color.White)

            drawLine(Color.Gray, Offset.Zero, Offset(size.width, size.height))
            drawRect(Color.Gray, style = Stroke(1.dp.toPx()))
            drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, 0f))

            val measureResult = textMeasurer.measure("BannerAd")
            drawText(
                textLayoutResult = measureResult,
                color = Color.Black,
                topLeft = center - Offset(
                    measureResult.size.width / 2f,
                    measureResult.size.height / 2f
                )
            )
        }
    )
}

@Composable
public fun BannerAdView(
    adState: AdState,
    modifier: Modifier = Modifier,
    isBannerCollapse: Boolean = false,
    token: BannerAdInstanceToken = rememberSaveable { UUID.randomUUID().toString() },
    onLoaded: (success: Boolean) -> Unit = { /* no-op */ },
) {
    if (LocalInspectionMode.current) {
        PreviewBannerAdView(modifier)
    } else {
        InternalBannerAdView(adState, token, onLoaded, modifier, isBannerCollapse)
    }
}

@Composable
private fun InternalBannerAdView(
    adState: AdState,
    token: BannerAdInstanceToken,
    onLoaded: (success: Boolean) -> Unit,
    modifier: Modifier,
    isBannerCollapse: Boolean,
    adAdapter: AdsAdapter = koinInject(),
) {
    val updatedOnLoaded by rememberUpdatedState(onLoaded)
    val appCompatActivity = LocalContext.current.findActivity() as AppCompatActivity

    var isBannerLoaded by remember(adState, token) {
        mutableStateOf(false)
    }
    var isLoadFailed by remember(adState, token) {
        mutableStateOf(false)
    }

    if (adState.enabled && !isLoadFailed) {
        val scope = rememberCoroutineScope()

        Column(modifier = Modifier.background(Color.White)) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.Gray)
            )
            AndroidView(
                factory = {
                    LayoutInflater.from(it).inflate(R.layout.layout_ad_banner, null, false)
                },
                modifier = modifier
            ) {
                if (isBannerLoaded) {
                    return@AndroidView
                }
                scope.launch {
                    it.findViewById<FrameLayout>(com.ads.control.R.id.fl_shimemr).z = -100f
                    it.destroyBannerView()
                    with(appCompatActivity) {
                        val loadSuccess = if (isBannerCollapse) {
                            adAdapter.loadBannerCollapse(
                                it,
                                CollapsibleGravity.BOTTOM,
                                adState.adId
                            )
                        } else {
                            adAdapter.loadBanner(it, adState.adId)
                        }
                        isLoadFailed = !loadSuccess
                        updatedOnLoaded(loadSuccess)
                    }
                }
                isBannerLoaded = true
            }
        }
    }
}
