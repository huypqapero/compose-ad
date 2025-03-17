package com.apero.core.ads.data.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ads.control.billing.AppPurchase
import com.ads.control.billing.PurchaseItem
import com.ads.control.funtion.PurchaseListener
import com.apero.core.ads.data.PurchaseError
import com.apero.core.ads.data.SubscriptionRepository
import com.apero.core.ads.data.model.InAppId
import com.apero.core.ads.data.model.SubscriptionId
import com.apero.core.coroutine.di.CoreCoroutineModule
import com.apero.core.util.`this`
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.coroutines.resume

@Single
@SuppressLint("LogNotTimber")
internal class SubscriptionRepositoryImpl(
    private val appPurchase: AppPurchase,
    @Named(CoreCoroutineModule.MAIN)
    private val mainDispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
) : SubscriptionRepository {

    private val _isPurchased = MutableStateFlow(appPurchase.isPurchased)
    override val isSubscribed: StateFlow<Boolean> = _isPurchased.asStateFlow()

    private val updatePurchaseListenerSignal = MutableSharedFlow<Unit>()

    private var ongoingPurchase: Pair<PurchaseItem, Job>? = null

    init {
        appPurchase.setPurchaseListener(object : PurchaseListener {
            override fun onProductPurchased(transactionId: String, transactionDetails: String) {
                appPurchase.updatePurchaseStatus()
            }

            override fun displayErrorMessage(p0: String?) {
                /* no-op */
            }

            override fun onUserCancelBilling() {
                Log.d(TAG, "onUserCancelBilling: ")
            }
        })
        appPurchase.setUpdatePurchaseListener {
            updateIsPurchased()
            updatePurchaseListenerSignal.tryEmit(Unit)
        }

        appPurchase.setBillingListener {
            updateIsPurchased()
        }
    }

    override suspend fun getPriceInApp(inAppId: InAppId): String = withContext(mainDispatcher) {
        appPurchase.getPrice(inAppId)
    }

    override suspend fun getPriceSubscription(subscriptionId: SubscriptionId): String =
        withContext(mainDispatcher) {
            appPurchase.getPriceSub(subscriptionId)
        }

    context(Activity)
    override suspend fun purchaseInApp(inAppId: InAppId): Either<PurchaseError, PurchaseItem> {
        return processPurchase(PurchaseItem(inAppId, AppPurchase.TYPE_IAP.PURCHASE))
    }

    context(Activity)
    override suspend fun purchaseSubscription(subscriptionId: SubscriptionId): Either<PurchaseError, PurchaseItem> {
        return processPurchase(PurchaseItem(subscriptionId, AppPurchase.TYPE_IAP.SUBSCRIPTION))
    }

    context(Activity)
    private suspend fun processPurchase(purchaseItem: PurchaseItem): Either<PurchaseError, PurchaseItem> {
        val currOngoingPurchase = ongoingPurchase
        if (purchaseItem == currOngoingPurchase?.first) {
            currOngoingPurchase.second.cancel()
        }
        return scope.async(mainDispatcher) {
            val resultOrNull = suspendCancellableCoroutine { continuation ->
                appPurchase.setPurchaseListener(object : PurchaseListener {
                    override fun onProductPurchased(
                        transactionId: String,
                        transactionDetails: String,
                    ) {
                        continuation.resume(purchaseItem.right()) {
                            appPurchase.setPurchaseListener(null)
                        }
                    }

                    override fun displayErrorMessage(p0: String?) {
                        continuation.resume(PurchaseError.DisplayUserMessage(p0).left())
                    }

                    override fun onUserCancelBilling() {
                        continuation.resume(PurchaseError.UserCancelBilling.left())
                    }
                })
                if (purchaseItem.type == AppPurchase.TYPE_IAP.SUBSCRIPTION) {
                    appPurchase.subscribe(`this`<Activity>(), purchaseItem.itemId)
                } else {
                    appPurchase.purchase(`this`<Activity>(), purchaseItem.itemId)
                }

                continuation.invokeOnCancellation {
                    appPurchase.setPurchaseListener(null)
                }
            }
            updateIsPurchased()
            resultOrNull
        }
            .also {
                ongoingPurchase = purchaseItem to it
            }
            .await()
    }

    override suspend fun consume(inAppId: InAppId) = coroutineScope {
        val job = launch {
            updatePurchaseListenerSignal.first()
        }
        appPurchase.consumePurchase(inAppId)
        job.join()
    }

    private fun updateIsPurchased() {
        _isPurchased.value = appPurchase.isPurchased
    }

    companion object {
        const val TAG = "SubscriptionRepository"
    }
}
