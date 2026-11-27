package id.twin.app.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap

data class CloneItem(
    val packageName: String,
    val userId: Int,
    val instanceNumber: Int,
    val appName: String,
    val customLabel: String?,
    val icon: ImageBitmap?,
    val rawBitmap: Bitmap?
) {
    val id: String get() = "$userId::$packageName"
    val displayName: String
        get() = customLabel ?: if (instanceNumber <= 1) appName else "$appName $instanceNumber"
}

data class HostAppItem(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val cloneCount: Int,
    val isSystemApp: Boolean
)

data class TwinSettings(
    val hostGmsCompat: Boolean = true,
    val hideRoot: Boolean = false,
    val keepAliveDaemon: Boolean = true,
    val disableFlagSecure: Boolean = false,
    val themeMode: Int = 0 // 0 = Sistem, 1 = Terang, 2 = Gelap
)
