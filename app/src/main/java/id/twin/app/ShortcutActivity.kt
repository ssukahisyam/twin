package id.twin.app

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import java.util.concurrent.Executors
import top.niunaijun.blackbox.BlackBoxCore

class ShortcutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent?.getStringExtra(EXTRA_PKG)
        val userId = intent?.getIntExtra(EXTRA_USER_ID, 0) ?: 0
        if (pkg.isNullOrBlank()) {
            finish()
            return
        }
        Executors.newSingleThreadExecutor().execute {
            val ok = runCatching {
                BlackBoxCore.get().waitForServicesAvailable(4000)
                BlackBoxCore.get().launchApk(pkg, userId)
            }.getOrDefault(false)
            runOnUiThread {
                if (!ok) {
                    Toast.makeText(this, "Gagal membuka aplikasi clone", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_PKG = "pkg"
        const val EXTRA_USER_ID = "userId"
    }
}
