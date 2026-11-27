package id.twin.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import id.twin.app.ui.TwinAppRoot
import id.twin.app.ui.TwinViewModel
import id.twin.app.ui.theme.TwinTheme
import id.twin.app.util.ShortcutHelper
import top.niunaijun.blackbox.BlackBoxCore

class MainActivity : ComponentActivity() {

    private val viewModel: TwinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching { BlackBoxCore.get().onBeforeMainActivityOnCreate(this) }
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.state.collectAsState()

            val notifPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _ -> }

            val apkPickerLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                if (uri != null) {
                    viewModel.addCloneFromApkUri(uri)
                }
            }

            TwinTheme(themeMode = uiState.settings.themeMode) {
                TwinAppRoot(
                    state = uiState,
                    onFinishOnboarding = {
                        viewModel.completeOnboarding()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            runCatching {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                    onLaunchClone = { viewModel.launchClone(it) },
                    onAddClone = { app, closeSheet -> viewModel.addClone(app, closeSheet) },
                    onPickApkFile = {
                        runCatching {
                            apkPickerLauncher.launch(arrayOf("*/*"))
                        }
                    },
                    onRenameClone = { clone, name -> viewModel.renameClone(clone, name) },
                    onPinShortcut = { clone ->
                        val ok = ShortcutHelper.pinShortcut(this, clone)
                        viewModel.showMessage(
                            if (ok) "Permintaan pintasan dikirim ke layar utama"
                            else "Launcher tidak mendukung pintasan otomatis"
                        )
                    },
                    onStopClone = { viewModel.stopClone(it) },
                    onClearCloneData = { viewModel.clearCloneData(it) },
                    onDeleteClone = { viewModel.deleteClone(it) },
                    onRefresh = { viewModel.refreshAll() },
                    onClearSnackbar = { viewModel.clearSnackbar() },
                    onToggleHostGms = { viewModel.updateHostGmsCompat(it) },
                    onToggleHideRoot = { viewModel.updateHideRoot(it) },
                    onToggleKeepAlive = { viewModel.updateKeepAliveDaemon(it) },
                    onToggleDisableFlagSecure = { viewModel.updateDisableFlagSecure(it) },
                    onChangeThemeMode = { viewModel.updateThemeMode(it) },
                    hasAllFilesAccess = { checkAllFilesAccess() },
                    onRequestAllFilesAccess = { openAllFilesAccessSettings() },
                    isIgnoringBatteryOptimizations = { checkIgnoringBatteryOptimizations() },
                    onRequestIgnoreBatteryOptimizations = { requestBatteryOptimizationExemption() }
                )
            }
        }

        runCatching { BlackBoxCore.get().onAfterMainActivityOnCreate(this) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAll()
    }

    private fun checkAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun openAllFilesAccessSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }.onFailure {
                runCatching {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
        }
    }

    private fun checkIgnoringBatteryOptimizations(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestBatteryOptimizationExemption() {
        runCatching {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }.onFailure {
            runCatching {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }
}
