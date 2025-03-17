package com.apero.core.ads.data

import android.app.Activity
import arrow.core.Either
import com.ads.control.billing.PurchaseItem
import com.apero.core.ads.data.model.InAppId
import com.apero.core.ads.data.model.SubscriptionId
import kotlinx.coroutines.flow.StateFlow

public interface SubscriptionRepository {
    public val isSubscribed: StateFlow<Boolean>

    public suspend fun getPriceInApp(inAppId: InAppId): String

    public suspend fun getPriceSubscription(subscriptionId: SubscriptionId): String

    context(Activity)
    public suspend fun purchaseInApp(
        inAppId: InAppId,
    ): Either<PurchaseError, PurchaseItem>

    context(Activity)
    public suspend fun purchaseSubscription(
        subscriptionId: SubscriptionId,
    ): Either<PurchaseError, PurchaseItem>

    public suspend fun consume(inAppId: InAppId)
}
