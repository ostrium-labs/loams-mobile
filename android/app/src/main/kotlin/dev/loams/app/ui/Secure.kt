package dev.loams.app.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/** FLAG_SECURE while this composable is shown: no screenshots, no recents thumbnail (AP2). */
@Composable
fun SecureScreen() {
    val activity = LocalContext.current as? Activity ?: return
    DisposableEffect(activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}
