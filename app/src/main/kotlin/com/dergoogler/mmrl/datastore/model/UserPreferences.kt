package com.dergoogler.mmrl.datastore.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ui.theme.Colors
import dev.dergoogler.mmrl.compat.BuildCompat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.InputStream
import java.io.OutputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class UserPreferences @OptIn(ExperimentalSerializationApi::class) constructor(
    @ProtoNumber(1) val workingMode: WorkingMode = WorkingMode.FIRST_SETUP,
    @ProtoNumber(2) val darkMode: DarkMode = DarkMode.FollowSystem,
    @ProtoNumber(3) val themeColor: Int = if (BuildCompat.atLeastS) Colors.Dynamic.id else Colors.MMRLBase.id,
    @ProtoNumber(4) val deleteZipFile: Boolean = false,
    @ProtoNumber(5) val downloadPath: String = Const.PUBLIC_DOWNLOADS.path,
    @ProtoNumber(6) val homepage: Homepage = Homepage.Home,
    @ProtoNumber(7) val repositoryMenu: RepositoryMenu = RepositoryMenu(),
    @ProtoNumber(8) val modulesMenu: ModulesMenu = ModulesMenu(),
    @ProtoNumber(9) val repositoriesMenu: RepositoriesMenu = RepositoriesMenu(),
    @ProtoNumber(10) val useDoh: Boolean = false,
    @ProtoNumber(11) val confirmReboot: Boolean = true,
    @ProtoNumber(12) val terminalTextWrap: Boolean = false,
    @ProtoNumber(13) val datePattern: String = "d MMMM yyyy",
    @Deprecated("Replaced by RepositoryService.isActive")
    @ProtoNumber(14) val autoUpdateRepos: Boolean = false,
    @ProtoNumber(15) val autoUpdateReposInterval: Long = 6,
    @Deprecated("Replaced by ModuleService.isActive")
    @ProtoNumber(16) val checkModuleUpdates: Boolean = false,
    @ProtoNumber(17) val checkModuleUpdatesInterval: Long = 6,
    @ProtoNumber(18) val checkAppUpdates: Boolean = true,
    @ProtoNumber(19) val checkAppUpdatesPreReleases: Boolean = false,
    @ProtoNumber(20) val hideFingerprintInHome: Boolean = true,
    @ProtoNumber(21) val webUiDevUrl: String = "https://127.0.0.1:8080",
    @ProtoNumber(22) val developerMode: Boolean = false,
    @ProtoNumber(23) val useWebUiDevUrl: Boolean = false,
    @ProtoNumber(24) val useShellForModuleStateChange: Boolean = false,
    @ProtoNumber(25) val useShellForModuleAction: Boolean = true,
    @ProtoNumber(26) val clearInstallTerminal: Boolean = true,
    @ProtoNumber(27) val allowCancelInstall: Boolean = false,
    @ProtoNumber(28) val allowCancelAction: Boolean = false,
    @ProtoNumber(29) val blacklistAlerts: Boolean = true,
    @ProtoNumber(30) val injectEruda: List<String> = emptyList(),
    @ProtoNumber(31) val allowedFsModules: List<String> = emptyList(),
    @ProtoNumber(32) val allowedKsuModules: List<String> = emptyList(),
    @Deprecated("Replaced by ProviderService.isActive")
    @ProtoNumber(33) val useProviderAsBackgroundService: Boolean = false,
) {
    @Composable
    fun isDarkMode() = when (darkMode) {
        DarkMode.AlwaysOff -> false
        DarkMode.AlwaysOn -> true
        DarkMode.FollowSystem -> isSystemInDarkTheme()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun encodeTo(output: OutputStream) = output.write(
        ProtoBuf.encodeToByteArray(this)
    )

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun decodeFrom(input: InputStream): UserPreferences =
            ProtoBuf.decodeFromByteArray(input.readBytes())
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> UserPreferences.developerMode(block: UserPreferences.() -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (developerMode) block() else null
}

@OptIn(ExperimentalContracts::class)
inline fun <R> UserPreferences.developerMode(
    also: UserPreferences.() -> Boolean,
    block: UserPreferences.() -> R,
): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (developerMode && also()) block() else null
}

@OptIn(ExperimentalContracts::class)
inline fun UserPreferences.developerMode(
    also: UserPreferences.() -> Boolean,
): Boolean {
    contract {
        callsInPlace(also, InvocationKind.AT_MOST_ONCE)
    }

    return developerMode && also()
}

@OptIn(ExperimentalContracts::class)
inline fun <R> UserPreferences.developerMode(
    also: UserPreferences.() -> Boolean,
    default: R,
    block: UserPreferences.() -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (developerMode && also()) block() else default
}