package id.twin.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.twin.app.data.CloneItem
import id.twin.app.data.CloneRepository
import id.twin.app.data.HostAppItem
import id.twin.app.data.TwinPrefs
import id.twin.app.data.TwinSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TwinUiState(
    val showOnboarding: Boolean = false,
    val isLoading: Boolean = true,
    val busyMessage: String? = null,
    val clones: List<CloneItem> = emptyList(),
    val hostApps: List<HostAppItem> = emptyList(),
    val settings: TwinSettings = TwinSettings(),
    val snackbarMessage: String? = null
)

class TwinViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val repository = CloneRepository(appContext)

    private val _state = MutableStateFlow(
        TwinUiState(
            showOnboarding = !TwinPrefs.isOnboardingDone(appContext),
            settings = TwinPrefs.getSettings(appContext)
        )
    )
    val state: StateFlow<TwinUiState> = _state.asStateFlow()

    init {
        refreshAll()
    }

    fun completeOnboarding() {
        TwinPrefs.setOnboardingDone(appContext, true)
        _state.update { it.copy(showOnboarding = false) }
    }

    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(msg: String) {
        _state.update { it.copy(snackbarMessage = msg) }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val clones = repository.loadClones()
            val hostApps = repository.loadHostApps(clones)
            _state.update {
                it.copy(
                    isLoading = false,
                    clones = clones,
                    hostApps = hostApps,
                    settings = TwinPrefs.getSettings(appContext)
                )
            }
        }
    }

    fun addClone(app: HostAppItem, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(busyMessage = "Membuat clone ${app.appName}…") }
            val res = repository.createClone(app.packageName)
            val clones = repository.loadClones()
            val hostApps = repository.loadHostApps(clones)
            _state.update {
                it.copy(
                    busyMessage = null,
                    clones = clones,
                    hostApps = hostApps,
                    snackbarMessage = res.fold(
                        onSuccess = { "${app.appName} berhasil di-clone" },
                        onFailure = { err -> "Gagal: ${err.message}" }
                    )
                )
            }
            if (res.isSuccess) onDone()
        }
    }

    fun addCloneFromApkUri(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(busyMessage = "Memasang file APK ke ruang clone…") }
            val res = repository.createCloneFromUri(uri)
            val clones = repository.loadClones()
            val hostApps = repository.loadHostApps(clones)
            _state.update {
                it.copy(
                    busyMessage = null,
                    clones = clones,
                    hostApps = hostApps,
                    snackbarMessage = res.fold(
                        onSuccess = { "APK berhasil dipasang ke Twin" },
                        onFailure = { err -> "Gagal: ${err.message}" }
                    )
                )
            }
        }
    }

    fun launchClone(clone: CloneItem) {
        viewModelScope.launch {
            _state.update { it.copy(busyMessage = "Membuka ${clone.displayName}…") }
            val ok = repository.launchClone(clone.packageName, clone.userId)
            _state.update {
                it.copy(
                    busyMessage = null,
                    snackbarMessage = if (ok) null else "Gagal membuka ${clone.displayName}"
                )
            }
        }
    }

    fun renameClone(clone: CloneItem, newLabel: String) {
        TwinPrefs.setCustomLabel(appContext, clone.packageName, clone.userId, newLabel)
        refreshAll()
    }

    fun stopClone(clone: CloneItem) {
        viewModelScope.launch {
            repository.stopClone(clone.packageName, clone.userId)
            _state.update { it.copy(snackbarMessage = "${clone.displayName} dihentikan") }
        }
    }

    fun clearCloneData(clone: CloneItem) {
        viewModelScope.launch {
            _state.update { it.copy(busyMessage = "Menghapus data ${clone.displayName}…") }
            repository.clearCloneData(clone.packageName, clone.userId)
            _state.update {
                it.copy(
                    busyMessage = null,
                    snackbarMessage = "Data ${clone.displayName} direset"
                )
            }
        }
    }

    fun deleteClone(clone: CloneItem) {
        viewModelScope.launch {
            _state.update { it.copy(busyMessage = "Menghapus ${clone.displayName}…") }
            repository.deleteClone(clone.packageName, clone.userId)
            val clones = repository.loadClones()
            val hostApps = repository.loadHostApps(clones)
            _state.update {
                it.copy(
                    busyMessage = null,
                    clones = clones,
                    hostApps = hostApps,
                    snackbarMessage = "${clone.displayName} dihapus"
                )
            }
        }
    }

    fun updateHostGmsCompat(enabled: Boolean) {
        TwinPrefs.setHostGmsCompat(appContext, enabled)
        _state.update { it.copy(settings = TwinPrefs.getSettings(appContext)) }
    }

    fun updateHideRoot(enabled: Boolean) {
        TwinPrefs.setHideRoot(appContext, enabled)
        _state.update { it.copy(settings = TwinPrefs.getSettings(appContext)) }
    }

    fun updateKeepAliveDaemon(enabled: Boolean) {
        TwinPrefs.setDaemonEnabled(appContext, enabled)
        _state.update { it.copy(settings = TwinPrefs.getSettings(appContext)) }
    }

    fun updateDisableFlagSecure(enabled: Boolean) {
        TwinPrefs.setDisableFlagSecure(appContext, enabled)
        _state.update { it.copy(settings = TwinPrefs.getSettings(appContext)) }
    }

    fun updateThemeMode(mode: Int) {
        TwinPrefs.setThemeMode(appContext, mode)
        _state.update { it.copy(settings = TwinPrefs.getSettings(appContext)) }
    }
}
