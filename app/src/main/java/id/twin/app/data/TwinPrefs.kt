package id.twin.app.data

import android.content.Context
import android.content.SharedPreferences

object TwinPrefs {
    private const val LABELS_PREFS = "twin_clone_labels"

    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_HOST_SIGNING_FALLBACK = "host_signing_fallback"
    private const val KEY_HIDE_ROOT = "hide_root"
    private const val KEY_DAEMON_ENABLE = "keep_alive_daemon"
    private const val KEY_DISABLE_FLAG_SECURE = "disable_flag_secure"
    private const val KEY_THEME_MODE = "theme_mode"

    private fun mainPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    private fun labelPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(LABELS_PREFS, Context.MODE_PRIVATE)

    fun ensureDefaults(context: Context) {
        val sp = mainPrefs(context)
        if (!sp.contains(KEY_HOST_SIGNING_FALLBACK)) {
            sp.edit()
                .putBoolean(KEY_HOST_SIGNING_FALLBACK, true)
                .putBoolean(KEY_DAEMON_ENABLE, true)
                .apply()
        }
    }

    fun isOnboardingDone(context: Context): Boolean =
        mainPrefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context, done: Boolean) {
        mainPrefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()
    }

    fun isHostGmsCompat(context: Context): Boolean =
        mainPrefs(context).getBoolean(KEY_HOST_SIGNING_FALLBACK, true)

    fun setHostGmsCompat(context: Context, enabled: Boolean) {
        mainPrefs(context).edit().putBoolean(KEY_HOST_SIGNING_FALLBACK, enabled).commit()
    }

    fun isHideRoot(context: Context): Boolean =
        mainPrefs(context).getBoolean(KEY_HIDE_ROOT, false)

    fun setHideRoot(context: Context, enabled: Boolean) {
        mainPrefs(context).edit().putBoolean(KEY_HIDE_ROOT, enabled).commit()
    }

    fun isDaemonEnabled(context: Context): Boolean =
        mainPrefs(context).getBoolean(KEY_DAEMON_ENABLE, true)

    fun setDaemonEnabled(context: Context, enabled: Boolean) {
        mainPrefs(context).edit().putBoolean(KEY_DAEMON_ENABLE, enabled).commit()
    }

    fun isDisableFlagSecure(context: Context): Boolean =
        mainPrefs(context).getBoolean(KEY_DISABLE_FLAG_SECURE, false)

    fun setDisableFlagSecure(context: Context, enabled: Boolean) {
        mainPrefs(context).edit().putBoolean(KEY_DISABLE_FLAG_SECURE, enabled).commit()
    }

    fun getThemeMode(context: Context): Int =
        mainPrefs(context).getInt(KEY_THEME_MODE, 0)

    fun setThemeMode(context: Context, mode: Int) {
        mainPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getSettings(context: Context): TwinSettings = TwinSettings(
        hostGmsCompat = isHostGmsCompat(context),
        hideRoot = isHideRoot(context),
        keepAliveDaemon = isDaemonEnabled(context),
        disableFlagSecure = isDisableFlagSecure(context),
        themeMode = getThemeMode(context)
    )

    private fun cloneKey(packageName: String, userId: Int): String = "$userId::$packageName"

    fun getCustomLabel(context: Context, packageName: String, userId: Int): String? =
        labelPrefs(context).getString(cloneKey(packageName, userId), null)?.takeIf { it.isNotBlank() }

    fun setCustomLabel(context: Context, packageName: String, userId: Int, label: String?) {
        val clean = label?.trim().orEmpty()
        val editor = labelPrefs(context).edit()
        if (clean.isEmpty()) {
            editor.remove(cloneKey(packageName, userId))
        } else {
            editor.putString(cloneKey(packageName, userId), clean)
        }
        editor.apply()
    }
}
