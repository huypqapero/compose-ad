package com.apero.core.ads.ui.native.component

import android.view.View
import android.view.ViewStub
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun NativeAdElementDelegate(
    onBindDelegate: (View) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* no-op */ },
    content: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    val view: View = remember(context) {
        ViewStub(context)
    }
    LaunchedEffect(view, onBindDelegate) {
        view.let(onBindDelegate)
    }
    Box(
        contentAlignment = Alignment.Center,
        content = content,
        modifier = modifier
            .clickable {
                view.performClick()
                onClick()
            }
    )
}
