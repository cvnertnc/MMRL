package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.compat.MediaStoreCompat.copyToDir
import com.dergoogler.mmrl.compat.MediaStoreCompat.getPathForUri
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.tmpDir
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.BulkModule
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.dergoogler.mmrl.utils.initPlatform
import com.topjohnwu.superuser.CallbackList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class InstallViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : TerminalViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    val logfile get() = "Install_${LocalDateTime.now()}.log"

    init {
        Timber.d("InstallViewModel initialized")
    }

    suspend fun installModules(scope: CoroutineScope, uris: List<Uri>) {
        val userPreferences = userPreferencesRepository.data.first()
        event = Event.LOADING
        var allSucceeded = true

        devLog(R.string.waiting_for_platformmanager_to_initialize)

        val deferred = initPlatform(
            scope = scope,
            context = context,
            platform = userPreferences.workingMode.toPlatform()
        )

        if (!deferred.await()) {
            event = Event.FAILED
            log(R.string.failed_to_initialize_platform)
            return
        } else {
            devLog(R.string.platform_initialized)
        }

        if (moduleManager == null) {
            event = Event.FAILED
            log(R.string.module_manager_is_null)
            return
        }

        if (fileManager == null) {
            event = Event.FAILED
            log(R.string.file_manager_is_null)
            return
        }

        val bulkModules = uris.mapNotNull { uri ->
            val path = context.getPathForUri(uri)

            if (path == null) {
                devLog(R.string.unable_to_find_path_for_uri, uri)
                return@mapNotNull null
            }

            if (userPreferences.strictMode && !path.endsWith(".zip")) {
                log(
                    R.string.is_not_a_module_file_magisk_modules_must_be_zip_files_skipping,
                    path
                )
                return@mapNotNull null
            }

            val info = PlatformManager.moduleManager.getModuleInfo(path)

            if (info == null) {
                devLog(R.string.unable_to_gather_module_info_of_file, path)
                return@mapNotNull null
            }

            val blacklist = getBlacklistById(info.id.toString())
            val isBlacklisted = Blacklist.isBlacklisted(userPreferences.blacklistAlerts, blacklist)
            if (isBlacklisted) {
                event = Event.FAILED
                allSucceeded = false
                log(R.string.cannot_install_blacklisted_modules_settings_security_blacklist_alerts)
                return
            }

            BulkModule(
                id = info.id.toString(),
                name = info.name
            )
        }

        for (uri in uris) {
            if (userPreferences.clearInstallTerminal && uris.size > 1) {
                console.clear()
            }

            val result = loadAndInstallModule(uri, bulkModules)
            if (!result) {
                allSucceeded = false
                log(context.getString(R.string.installation_aborted_due_to_an_error))
                break
            }
        }

        event = if (allSucceeded) {
            Event.SUCCEEDED
        } else {
            Event.FAILED
        }
    }

    private suspend fun loadAndInstallModule(
        uri: Uri,
        bulkModules: List<BulkModule>,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val path = context.getPathForUri(uri)

            if (path == null) {
                log(R.string.unable_to_find_path_for_uri, uri)
                return@withContext false
            }

            devLog(R.string.install_view_path, path)

            PlatformManager.moduleManager.getModuleInfo(path)?.let {
                devLog(R.string.install_view_module_info, it.toString())
                return@withContext install(path, bulkModules, it)
            }

            log(R.string.copying_zip_to_temp_directory)
            val tmpFile = context.copyToDir(uri, context.tmpDir) ?: run {
                event = Event.FAILED
                log(context.getString(R.string.copying_failed))
                return@withContext false
            }

            val io = context.contentResolver.openInputStream(uri)

            if (io == null) {
                event = Event.FAILED
                log(R.string.copying_failed)
                return@withContext false
            }

            io.use { input ->
                tmpFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val moduleInfo = PlatformManager.moduleManager.getModuleInfo(tmpFile.path)

            if (moduleInfo == null) {
                event = Event.FAILED
                log(R.string.unable_to_gather_module_info)
                return@withContext false
            }

            devLog(R.string.install_view_module_info)
            return@withContext install(tmpFile.path, bulkModules)
        }

    private suspend fun install(
        zipPath: String,
        bulkModules: List<BulkModule>,
        module: LocalModule? = null,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val zipFile = File(zipPath)
            val userPreferences = userPreferencesRepository.data.first()

            val installationResult = CompletableDeferred<Boolean>()

            val cmds = listOf(
                "export ASH_STANDALONE=1",
                "export MMRL=true",
                "export MMRL_VER=${BuildConfig.VERSION_NAME}",
                "export MMRL_VER_CODE=${BuildConfig.VERSION_CODE}",
                "export BULK_MODULES=\"${bulkModules.joinToString(" ") { it.id }}\"",
                moduleManager!!.getInstallCommand(zipPath)
            )

            val stdout = object : CallbackList<String?>() {
                override fun onAddElement(msg: String?) {
                    if (msg == null) return

                    viewModelScope.launch {
                        log(msg)
                    }
                }
            }

            val stderr = object : CallbackList<String?>() {
                override fun onAddElement(msg: String?) {
                    if (msg == null) return

                    viewModelScope.launch {
                        if (userPreferences.developerMode) console.add(msg)
                        logs.add(msg)
                    }
                }
            }

            log(R.string.install_view_installing, zipFile.name)

            val result = shell.newJob().add(*cmds.toTypedArray()).to(stdout, stderr).exec()

            if (result.isSuccess) {
                module.nullable(::insertLocal)
                if (userPreferences.deleteZipFile) {
                    deleteBySu(zipPath)
                }
                installationResult.complete(true)
            } else {
                if (module != null && !shell.isAlive) {
                    runCatching {
                        fileManager.nullable {
                            it.delete("/data/adb/modules_update/${module.id}")
                        }
                    }.onFailure {
                        Timber.e(it)
                        log(R.string.failed_to_remove_updated_folder)
                    }.onSuccess {
                        devLog(R.string.removed_updated_folder)
                    }
                }
                installationResult.complete(false)
            }

            return@withContext installationResult.await()
        }


    private fun insertLocal(module: LocalModule) {
        viewModelScope.launch {
            localRepository.insertLocal(module.copy(state = com.dergoogler.mmrl.platform.content.State.UPDATE))
        }
    }

    private fun deleteBySu(zipPath: String) {
        runCatching {
            PlatformManager.fileManager.deleteOnExit(zipPath)
        }.onFailure {
            Timber.e(it)
        }.onSuccess {
            Timber.d("Deleted: $zipPath")
        }
    }
}
