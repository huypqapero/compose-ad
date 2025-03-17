package com.apero.core.ads.data

import android.app.Activity
import arrow.core.Either
import arrow.core.raise.either
import com.apero.core.ads.data.model.InAppId

context(Activity)
public suspend fun SubscriptionRepository.purchaseThenConsumeInApp(
    inAppId: InAppId,
): Either<PurchaseError, Unit> {
    return either {
        purchaseInApp(inAppId).bind()
        consume(inAppId)
    }
}
