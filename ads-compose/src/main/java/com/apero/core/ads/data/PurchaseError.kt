package com.apero.core.ads.data

public sealed interface PurchaseError {
    public data object UserCancelBilling : PurchaseError

    @JvmInline
    public value class DisplayUserMessage(public val msg: String?) : PurchaseError
}
