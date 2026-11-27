package id.twin.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.core.GmsCore

class CloneRepository(private val context: Context) {

    private data class CachedIcon(val bitmap: Bitmap, val imageBitmap: ImageBitmap)
    private val iconCache = ConcurrentHashMap<String, CachedIcon>()

    private fun loadIcon(pm: PackageManager, appInfo: ApplicationInfo): CachedIcon? {
        iconCache[appInfo.packageName]?.let { return it }
        return runCatching {
            val drawable = appInfo.loadIcon(pm) ?: pm.defaultActivityIcon
            val bmp = drawableToBitmap(drawable, 144)
            CachedIcon(bmp, bmp.asImageBitmap()).also {
                iconCache[appInfo.packageName] = it
            }
        }.getOrNull()
    }

    private fun drawableToBitmap(drawable: Drawable, maxPx: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val b = drawable.bitmap
            if (b.width in 1..maxPx && b.height in 1..maxPx) {
                return b
            }
        }
        val w = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(maxPx) ?: maxPx
        val h = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(maxPx) ?: maxPx
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isInternalGmsPackage(pkg: String): Boolean {
        return GmsCore.isGoogleService(pkg) || pkg == GmsCore.VENDING_PKG
    }

    suspend fun loadClones(): List<CloneItem> = withContext(Dispatchers.IO) {
        val core = BlackBoxCore.get()
        core.waitForServicesAvailable(5000)
        val pm = context.packageManager

        data class RawEntry(
            val pkg: String,
            val userId: Int,
            val appName: String,
            val icon: CachedIcon?
        )

        val entries = mutableListOf<RawEntry>()
        val users = runCatching { core.users }.getOrDefault(emptyList()).sortedBy { it.id }

        for (user in users) {
            val installed = runCatching {
                core.getInstalledApplications(0, user.id)
            }.getOrDefault(emptyList())

            for (appInfo in installed) {
                val pkg = appInfo.packageName ?: continue
                if (pkg == context.packageName || isInternalGmsPackage(pkg)) continue

                val hostInfo = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull() ?: appInfo
                val label = runCatching {
                    hostInfo.loadLabel(pm).toString().takeIf { it.isNotBlank() }
                }.getOrNull() ?: pkg
                val cached = loadIcon(pm, hostInfo)
                entries.add(RawEntry(pkg, user.id, label, cached))
            }
        }

        val groupedCounts = HashMap<String, Int>()
        entries
            .sortedWith(compareBy<RawEntry> { it.appName.lowercase() }.thenBy { it.userId })
            .map { entry ->
                val instanceNum = (groupedCounts[entry.pkg] ?: 0) + 1
                groupedCounts[entry.pkg] = instanceNum
                CloneItem(
                    packageName = entry.pkg,
                    userId = entry.userId,
                    instanceNumber = instanceNum,
                    appName = entry.appName,
                    customLabel = TwinPrefs.getCustomLabel(context, entry.pkg, entry.userId),
                    icon = entry.icon?.imageBitmap,
                    rawBitmap = entry.icon?.bitmap
                )
            }
    }

    suspend fun loadHostApps(clones: List<CloneItem>): List<HostAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val countsByPkg = clones.groupingBy { it.packageName }.eachCount()

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolves = runCatching { pm.queryIntentActivities(launcherIntent, 0) }
            .getOrDefault(emptyList())

        val seen = HashSet<String>()
        val result = ArrayList<HostAppItem>()

        for (ri in resolves) {
            val ai = ri.activityInfo?.applicationInfo ?: continue
            val pkg = ai.packageName ?: continue
            if (!seen.add(pkg)) continue
            if (pkg == context.packageName) continue
            if (isInternalGmsPackage(pkg)) continue
            if (runCatching { BlackBoxCore.get().isBlackBoxApp(pkg) }.getOrDefault(false)) continue

            val name = runCatching { ai.loadLabel(pm).toString() }.getOrDefault(pkg)
            val isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
            val icon = loadIcon(pm, ai)

            result.add(
                HostAppItem(
                    packageName = pkg,
                    appName = name,
                    icon = icon?.imageBitmap,
                    cloneCount = countsByPkg[pkg] ?: 0,
                    isSystemApp = isSystem
                )
            )
        }

        result.sortedWith(
            compareBy<HostAppItem> { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
    }

    suspend fun createClone(packageName: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val core = BlackBoxCore.get()
            core.waitForServicesAvailable(5000)

            var targetUserId = 0
            while (targetUserId < 50 && core.isInstalled(packageName, targetUserId)) {
                targetUserId++
            }
            core.createUser(targetUserId)

            val res = core.installPackageAsUser(packageName, targetUserId)
            if (!res.success) {
                throw IllegalStateException(res.msg ?: "Gagal meng-clone aplikasi")
            }
            targetUserId
        }
    }

    suspend fun createCloneFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val core = BlackBoxCore.get()
            core.waitForServicesAvailable(5000)

            val existingUsers = runCatching { core.users.map { it.id } }.getOrDefault(emptyList())
            val nextUser = (existingUsers.maxOrNull() ?: -1) + 1
            val targetUserId = nextUser.coerceAtLeast(0)
            core.createUser(targetUserId)

            val res = core.installPackageAsUser(uri, targetUserId)
            if (!res.success) {
                cleanupEmptyUser(targetUserId)
                throw IllegalStateException(res.msg ?: "Gagal memasang file APK")
            }
            res.packageName ?: "APK"
        }
    }

    suspend fun launchClone(packageName: String, userId: Int): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val core = BlackBoxCore.get()
            core.waitForServicesAvailable(5000)
            core.launchApk(packageName, userId)
        }.getOrDefault(false)
    }

    suspend fun stopClone(packageName: String, userId: Int) = withContext(Dispatchers.IO) {
        runCatching { BlackBoxCore.get().stopPackage(packageName, userId) }
    }

    suspend fun clearCloneData(packageName: String, userId: Int) = withContext(Dispatchers.IO) {
        runCatching { BlackBoxCore.get().clearPackage(packageName, userId) }
    }

    suspend fun deleteClone(packageName: String, userId: Int) = withContext(Dispatchers.IO) {
        runCatching {
            val core = BlackBoxCore.get()
            core.stopPackage(packageName, userId)
            core.uninstallPackageAsUser(packageName, userId)
            TwinPrefs.setCustomLabel(context, packageName, userId, null)
            cleanupEmptyUser(userId)
        }
    }

    private fun cleanupEmptyUser(userId: Int) {
        runCatching {
            val core = BlackBoxCore.get()
            val remaining = core.getInstalledApplications(0, userId)
                .filterNot { isInternalGmsPackage(it.packageName.orEmpty()) }
            if (remaining.isEmpty()) {
                core.deleteUser(userId)
            }
        }
    }
}
