package id.twin.app

import android.app.Application
import android.content.Context
import android.util.Log
import id.twin.app.data.TwinPrefs
import java.io.File
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback
import top.niunaijun.blackbox.app.configuration.ClientConfiguration
import top.niunaijun.blackbox.fake.device.DeviceSpoofManager

class TwinApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val ctx = base ?: return
        appContext = ctx

        TwinPrefs.ensureDefaults(ctx)

        runCatching { BlackBoxCore.get().closeCodeInit() }
            .onFailure { Log.w(TAG, "closeCodeInit: ${it.message}") }

        runCatching { BlackBoxCore.get().onBeforeMainApplicationAttach(this, ctx) }
            .onFailure { Log.w(TAG, "onBeforeMainApplicationAttach: ${it.message}") }

        runCatching {
            BlackBoxCore.get().doAttachBaseContext(
                ctx,
                object : ClientConfiguration() {
                    override fun getHostPackageName(): String = ctx.packageName
                    override fun isHideRoot(): Boolean = TwinPrefs.isHideRoot(ctx)
                    override fun isEnableDaemonService(): Boolean = TwinPrefs.isDaemonEnabled(ctx)
                    override fun isUseVpnNetwork(): Boolean = false
                    override fun isDisableFlagSecure(): Boolean = TwinPrefs.isDisableFlagSecure(ctx)
                    override fun requestInstallPackage(file: File?, userId: Int): Boolean = false
                    override fun getLogSenderChatId(): String = ""
                }
            )
            BlackBoxCore.get().addAppLifecycleCallback(
                object : AppLifecycleCallback() {
                    override fun beforeCreateApplication(
                        packageName: String?,
                        processName: String?,
                        context: Context?,
                        userId: Int
                    ) {
                        runCatching { DeviceSpoofManager.applyToCurrentProcess(userId) }
                    }
                }
            )
        }.onFailure { Log.e(TAG, "doAttachBaseContext error", it) }

        runCatching { BlackBoxCore.get().onAfterMainApplicationAttach(this, ctx) }
            .onFailure { Log.w(TAG, "onAfterMainApplicationAttach: ${it.message}") }
    }

    override fun onCreate() {
        super.onCreate()
        runCatching { BlackBoxCore.get().doCreate() }
            .onFailure { Log.e(TAG, "BlackBoxCore.doCreate error", it) }
    }

    companion object {
        private const val TAG = "TwinApp"

        @Volatile
        lateinit var appContext: Context
            private set
    }
}
