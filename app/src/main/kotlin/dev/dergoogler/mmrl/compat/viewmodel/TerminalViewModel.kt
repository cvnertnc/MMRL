package dev.dergoogler.mmrl.compat.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.app.Const.CLEAR_CMD
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import com.dergoogler.mmrl.ui.activity.terminal.Actions
import com.dergoogler.mmrl.ui.activity.terminal.ShellBroadcastReceiver
import dev.dergoogler.mmrl.compat.BuildCompat
import dev.dergoogler.mmrl.compat.stub.IShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject


open class TerminalViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    internal val logs = mutableListOf<String>()
    internal val console = mutableStateListOf<String>()
    var event by mutableStateOf(Event.LOADING)
        internal set
    var shell by mutableStateOf<IShell?>(null)
        internal set

    private val localFlow = MutableStateFlow<LocalModule?>(null)
    val local get() = localFlow.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    fun registerReceiver() {
        if (receiver == null) {
            receiver = ShellBroadcastReceiver(context, console, logs)

            val filter = IntentFilter().apply {
                addAction(Actions.SET_LAST_LINE)
                addAction(Actions.REMOVE_LAST_LINE)
                addAction(Actions.CLEAR_TERMINAL)
                addAction(Actions.LOG)
            }

            if (BuildCompat.atLeastT) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, filter)
            }
        }
    }

    fun unregisterReceiver() {
        if (receiver == null) {
            Timber.w("ShellBroadcastReceiver is already null")
            return
        }

        context.unregisterReceiver(receiver)
        receiver = null
    }

    private fun IntentFilter.addAction(action: Actions) {
        addAction("${context.packageName}.${action.name}")
    }

    suspend fun writeLogsTo(uri: Uri) = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(logs.joinToString(separator = "\n").toByteArray())
            }
        }.onFailure {
            Timber.e(it)
        }
    }


    private val localizedEnglishResources
        get(): Resources {
            var conf: Configuration = context.resources.configuration
            conf = Configuration(conf)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = context.createConfigurationContext(conf)
            return localizedContext.resources
        }

    private val devMode = runBlocking { userPreferencesRepository.data.first().developerMode }
    internal fun devLog(@StringRes message: Int, vararg format: Any?) {
        Timber.d(localizedEnglishResources.getString(message, *format))
        if (devMode) log(message, *format)
    }

    internal fun log(@StringRes message: Int, vararg format: Any?) {
        val serializedFormat: Array<out String> = format.map { it.toString() }.toTypedArray()
        log(
            message = context.getString(message, *serializedFormat),
            log = localizedEnglishResources.getString(message, *serializedFormat)
        )
    }

    internal fun log(
        message: String,
        log: String = message,
    ) {
        if (message.startsWith(CLEAR_CMD)) {
            console.clear()
        } else {
            console.add(message)
            logs.add(log)
        }
    }
}
