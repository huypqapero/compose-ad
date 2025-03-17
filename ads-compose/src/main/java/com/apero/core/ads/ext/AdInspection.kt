package com.apero.core.ads.ext

import timber.log.Timber

/**
 * Created by KO Huyn on 07/05/2024.
 */
public object AdInspection {
    public var isEnableMessageForTester: Boolean = false
        set(value) {
            Timber.d("isEnableMessageForTester:$value")
            field = value
        }
    public enum class Mode {
        SelectMediation, ToastAdUnitId
    }
}