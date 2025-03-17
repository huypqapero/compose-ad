package com.apero.core.ads.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apero.core.ads.data.SubscriptionRepository
import com.apero.core.ads.data.model.AdUnitId
import org.koin.compose.koinInject

@Composable
public fun rememberAdState(
    adId: AdUnitId,
    debugName: String = "",
    isSubscribed: State<Boolean> = rememberIsSubscribed(),
): AdState {
    val state = remember(
        adId,
        isSubscribed,
    ) {
        AdState(debugName, adId, adId.isEnableShowAd, isSubscribed)
    }

    return state
}

@Composable
internal fun rememberIsSubscribed(): State<Boolean> {
    if (LocalInspectionMode.current) {
        return rememberSaveable {
            mutableStateOf(false)
        }
    }

    val subscriptionRepository: SubscriptionRepository = koinInject()
    return subscriptionRepository.isSubscribed.collectAsStateWithLifecycle()
}

@Stable
public class AdState(
    public val debugName: String,
    public val adId: AdUnitId,
    public val remoteEnabled: Boolean,
    private val isSubscribed: State<Boolean>,
) {

    public val enabled: Boolean
        get() {
            return !isSubscribed.value && remoteEnabled
        }
}