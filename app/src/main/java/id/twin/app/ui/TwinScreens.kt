package id.twin.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.twin.app.BuildConfig
import id.twin.app.data.CloneItem
import id.twin.app.data.HostAppItem
import id.twin.app.data.TwinSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwinAppRoot(
    state: TwinUiState,
    onFinishOnboarding: () -> Unit,
    onLaunchClone: (CloneItem) -> Unit,
    onAddClone: (HostAppItem, () -> Unit) -> Unit,
    onPickApkFile: () -> Unit,
    onRenameClone: (CloneItem, String) -> Unit,
    onPinShortcut: (CloneItem) -> Unit,
    onStopClone: (CloneItem) -> Unit,
    onClearCloneData: (CloneItem) -> Unit,
    onDeleteClone: (CloneItem) -> Unit,
    onRefresh: () -> Unit,
    onClearSnackbar: () -> Unit,
    onToggleHostGms: (Boolean) -> Unit,
    onToggleHideRoot: (Boolean) -> Unit,
    onToggleKeepAlive: (Boolean) -> Unit,
    onToggleDisableFlagSecure: (Boolean) -> Unit,
    onChangeThemeMode: (Int) -> Unit,
    hasAllFilesAccess: () -> Boolean,
    onRequestAllFilesAccess: () -> Unit,
    isIgnoringBatteryOptimizations: () -> Boolean,
    onRequestIgnoreBatteryOptimizations: () -> Unit
) {
    if (state.showOnboarding) {
        OnboardingScreen(onStart = onFinishOnboarding)
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var selectedClone by remember { mutableStateOf<CloneItem?>(null) }
    var renameTarget by remember { mutableStateOf<CloneItem?>(null) }
    var deleteTarget by remember { mutableStateOf<CloneItem?>(null) }
    var resetTarget by remember { mutableStateOf<CloneItem?>(null) }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onClearSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TwinLogoMark(size = 26.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Twin",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.clones.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Tambah clone")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && state.clones.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.5.dp)
                    }
                }

                state.clones.isEmpty() -> {
                    EmptyHomeState(onAddClick = { showAddSheet = true })
                }

                else -> {
                    CloneGrid(
                        clones = state.clones,
                        onTap = onLaunchClone,
                        onLongPress = { selectedClone = it }
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddCloneBottomSheet(
            apps = state.hostApps,
            onDismiss = { showAddSheet = false },
            onSelectApp = { app ->
                onAddClone(app) { showAddSheet = false }
            },
            onPickApk = {
                showAddSheet = false
                onPickApkFile()
            }
        )
    }

    selectedClone?.let { clone ->
        CloneActionsBottomSheet(
            clone = clone,
            onDismiss = { selectedClone = null },
            onRename = {
                selectedClone = null
                renameTarget = clone
            },
            onPinShortcut = {
                selectedClone = null
                onPinShortcut(clone)
            },
            onStop = {
                selectedClone = null
                onStopClone(clone)
            },
            onResetData = {
                selectedClone = null
                resetTarget = clone
            },
            onDelete = {
                selectedClone = null
                deleteTarget = clone
            }
        )
    }

    renameTarget?.let { clone ->
        RenameCloneDialog(
            clone = clone,
            onDismiss = { renameTarget = null },
            onConfirm = { newName ->
                onRenameClone(clone, newName)
                renameTarget = null
            }
        )
    }

    deleteTarget?.let { clone ->
        ConfirmActionDialog(
            title = "Hapus ${clone.displayName}?",
            message = "Clone ini beserta semua data akun di dalamnya akan dihapus permanen. Aplikasi asli di HP tidak terpengaruh.",
            confirmLabel = "Hapus",
            isDestructive = true,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDeleteClone(clone)
                deleteTarget = null
            }
        )
    }

    resetTarget?.let { clone ->
        ConfirmActionDialog(
            title = "Reset data ${clone.displayName}?",
            message = "Semua data & sesi login di dalam clone ini akan dibersihkan seperti baru dipasang.",
            confirmLabel = "Reset",
            isDestructive = true,
            onDismiss = { resetTarget = null },
            onConfirm = {
                onClearCloneData(clone)
                resetTarget = null
            }
        )
    }

    if (showSettingsSheet) {
        SettingsBottomSheet(
            settings = state.settings,
            hasAllFiles = hasAllFilesAccess(),
            ignoringBattery = isIgnoringBatteryOptimizations(),
            onDismiss = {
                showSettingsSheet = false
                onRefresh()
            },
            onToggleHostGms = onToggleHostGms,
            onToggleHideRoot = onToggleHideRoot,
            onToggleKeepAlive = onToggleKeepAlive,
            onToggleDisableFlagSecure = onToggleDisableFlagSecure,
            onChangeThemeMode = onChangeThemeMode,
            onRequestAllFiles = onRequestAllFilesAccess,
            onRequestIgnoreBattery = onRequestIgnoreBatteryOptimizations
        )
    }

    if (state.busyMessage != null) {
        BusyDialog(message = state.busyMessage)
    }
}

@Composable
private fun TwinLogoMark(size: Dp) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(size)) {
        val r = this.size.minDimension * 0.31f
        val stroke = Stroke(width = this.size.minDimension * 0.11f)
        val cy = this.size.height / 2f
        drawCircle(
            color = primary,
            radius = r,
            center = Offset(this.size.width * 0.37f, cy),
            style = stroke
        )
        drawCircle(
            color = primary.copy(alpha = 0.65f),
            radius = r,
            center = Offset(this.size.width * 0.63f, cy),
            style = stroke
        )
    }
}

@Composable
private fun OnboardingScreen(onStart: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(92.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        TwinLogoMark(size = 54.dp)
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "Satu HP, banyak akun.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Jalankan beberapa akun dari aplikasi yang sama dalam ruang terpisah — ringan, bersih, tanpa iklan.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Mulai",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHomeState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                TwinLogoMark(size = 40.dp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Belum ada clone",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tambahkan aplikasi untuk menjalankan akun kedua, ketiga, dan seterusnya.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 13.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tambah aplikasi", fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CloneGrid(
    clones: List<CloneItem>,
    onTap: (CloneItem) -> Unit,
    onLongPress: (CloneItem) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${clones.size} aplikasi aktif · tahan ikon untuk opsi",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(clones, key = { it.id }) { clone ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .combinedClickable(
                            onClick = { onTap(clone) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPress(clone)
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BadgedAppIcon(
                            icon = clone.icon,
                            badgeNumber = clone.instanceNumber,
                            iconSize = 54.dp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = clone.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgedAppIcon(
    icon: ImageBitmap?,
    badgeNumber: Int,
    iconSize: Dp
) {
    Box(modifier = Modifier.size(iconSize + 6.dp), contentAlignment = Alignment.Center) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(14.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(21.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(2.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeNumber.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 10.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCloneBottomSheet(
    apps: List<HostAppItem>,
    onDismiss: () -> Unit,
    onSelectApp: (HostAppItem) -> Unit,
    onPickApk: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    var showSystemApps by rememberSaveable { mutableStateOf(false) }

    val filtered = remember(apps, query, showSystemApps) {
        val q = query.trim().lowercase()
        apps.filter { app ->
            (showSystemApps || !app.isSystemApp || q.isNotEmpty()) &&
                    (q.isEmpty() || app.appName.lowercase().contains(q) || app.packageName.lowercase().contains(q))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pilih aplikasi",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onPickApk) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Dari APK", fontSize = 13.sp)
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cari aplikasi…", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Bersihkan", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showSystemApps,
                    onClick = { showSystemApps = !showSystemApps },
                    label = { Text("Tampilkan aplikasi sistem", fontSize = 12.sp) }
                )
            }

            Spacer(Modifier.height(4.dp))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (apps.isEmpty()) "Memuat daftar aplikasi…" else "Aplikasi tidak ditemukan",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectApp(app) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.icon != null) {
                                Image(
                                    bitmap = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (app.cloneCount > 0) {
                                    Text(
                                        text = "${app.cloneCount} clone terpasang · ketuk untuk tambah lagi",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = "Clone",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CloneActionsBottomSheet(
    clone: CloneItem,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onPinShortcut: () -> Unit,
    onStop: () -> Unit,
    onResetData: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgedAppIcon(icon = clone.icon, badgeNumber = clone.instanceNumber, iconSize = 46.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = clone.displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${clone.appName} · Clone #${clone.instanceNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SheetActionRow(icon = Icons.Outlined.Edit, label = "Ganti nama clone", onClick = onRename)
            SheetActionRow(icon = Icons.Outlined.Home, label = "Tambah ke layar utama", onClick = onPinShortcut)
            SheetActionRow(icon = Icons.Outlined.StopCircle, label = "Hentikan paksa", onClick = onStop)
            SheetActionRow(icon = Icons.Outlined.RestartAlt, label = "Reset data clone", onClick = onResetData)
            SheetActionRow(
                icon = Icons.Outlined.DeleteOutline,
                label = "Hapus clone",
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = label, fontSize = 15.sp, color = tint, fontWeight = FontWeight.Normal)
    }
}

@Composable
private fun RenameCloneDialog(
    clone: CloneItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf(clone.displayName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti nama clone", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(if (clone.instanceNumber <= 1) clone.appName else "${clone.appName} ${clone.instanceNumber}")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Kosongkan untuk kembali ke nama bawaan.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Simpan", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    isDestructive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
        text = { Text(message, fontSize = 14.sp, lineHeight = 20.sp) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.textButtonColors()
                }
            ) {
                Text(confirmLabel, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsBottomSheet(
    settings: TwinSettings,
    hasAllFiles: Boolean,
    ignoringBattery: Boolean,
    onDismiss: () -> Unit,
    onToggleHostGms: (Boolean) -> Unit,
    onToggleHideRoot: (Boolean) -> Unit,
    onToggleKeepAlive: (Boolean) -> Unit,
    onToggleDisableFlagSecure: (Boolean) -> Unit,
    onChangeThemeMode: (Int) -> Unit,
    onRequestAllFiles: () -> Unit,
    onRequestIgnoreBattery: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Pengaturan",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Tampilan",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Ikuti Sistem", 1 to "Terang", 2 to "Gelap").forEach { (mode, label) ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { onChangeThemeMode(mode) },
                        label = { Text(label, fontSize = 13.sp) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Kompatibilitas & Ruang Clone",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))

            SettingSwitchItem(
                title = "Layanan Google Play & Meta otomatis",
                subtitle = "Meneruskan Google Play Services & sertifikat asli HP agar aplikasi seperti Muse dan Instagram tidak error",
                checked = settings.hostGmsCompat,
                onCheckedChange = onToggleHostGms
            )

            SettingSwitchItem(
                title = "Jaga notifikasi latar belakang",
                subtitle = "Menjaga proses layanan clone tetap aktif agar notifikasi masuk",
                checked = settings.keepAliveDaemon,
                onCheckedChange = onToggleKeepAlive
            )

            SettingSwitchItem(
                title = "Sembunyikan status Root",
                subtitle = "Bantu menyamarkan indikator root di dalam ruang clone",
                checked = settings.hideRoot,
                onCheckedChange = onToggleHideRoot
            )

            SettingSwitchItem(
                title = "Izinkan tangkapan layar",
                subtitle = "Abaikan pembatasan layar hitam saat screenshot di aplikasi clone",
                checked = settings.disableFlagSecure,
                onCheckedChange = onToggleDisableFlagSecure
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Izin Pendukung",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = !ignoringBattery) {
                OutlinedButton(
                    onClick = onRequestIgnoreBattery,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Outlined.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Kecualikan dari penghemat baterai")
                }
            }

            AnimatedVisibility(visible = !hasAllFiles) {
                OutlinedButton(
                    onClick = onRequestAllFiles,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Berikan akses penyimpanan penuh")
                }
            }

            if (ignoringBattery && hasAllFiles) {
                Text(
                    text = "Semua izin optimal sudah aktif.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(22.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))

            Text(
                text = "Twin v${BuildConfig.VERSION_NAME} · Tanpa iklan, tanpa pelacak.\nEngine berbasis proyek open-source BlackBox (Apache 2.0).",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BusyDialog(message: String) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
