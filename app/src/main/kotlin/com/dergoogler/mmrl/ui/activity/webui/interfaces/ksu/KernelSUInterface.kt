package com.dergoogler.mmrl.ui.activity.webui.interfaces.ksu

import android.content.Context
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.datastore.UserPreferencesCompat
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.ShellUtils
import com.topjohnwu.superuser.internal.UiThreadHandler
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

class AdvancedKernelSUAPI(
    context: Context,
    webView: WebView,
    private val userPrefs: UserPreferencesCompat,
) : BaseKernelSUAPI(context, webView) {

    @JavascriptInterface
    fun exec(cmd: String): String {
        return Compat.withNewRootShell(
            globalMnt = true,
            devMode = userPrefs.developerMode
        ) { ShellUtils.fastCmd(this, cmd) }
    }

    @JavascriptInterface
    fun exec(cmd: String, callbackFunc: String) {
        exec(cmd, null, callbackFunc)
    }

    private fun processOptions(sb: StringBuilder, options: String?) {
        val opts = if (options == null) JSONObject() else {
            JSONObject(options)
        }

        val cwd = opts.optString("cwd")
        if (!TextUtils.isEmpty(cwd)) {
            sb.append("cd ${cwd};")
        }

        opts.optJSONObject("env")?.let { env ->
            env.keys().forEach { key ->
                sb.append("export ${key}=${env.getString(key)};")
            }
        }
    }

    @JavascriptInterface
    fun exec(
        cmd: String,
        options: String?,
        callbackFunc: String,
    ) {
        val finalCommand = StringBuilder()
        processOptions(finalCommand, options)
        finalCommand.append(cmd)

        val result = Compat.withNewRootShell(
            globalMnt = true,
            devMode = userPrefs.developerMode
        ) {
            newJob().add(finalCommand.toString()).to(ArrayList(), ArrayList()).exec()
        }
        val stdout = result.out.joinToString(separator = "\n")
        val stderr = result.err.joinToString(separator = "\n")

        val jsCode =
            "(function() { try { ${callbackFunc}(${result.code}, ${
                JSONObject.quote(
                    stdout
                )
            }, ${JSONObject.quote(stderr)}); } catch(e) { console.error(e); } })();"

        runJs(jsCode)
    }

    @JavascriptInterface
    fun spawn(command: String, args: String, options: String?, callbackFunc: String) {
        val finalCommand = StringBuilder()

        processOptions(finalCommand, options)

        if (!TextUtils.isEmpty(args)) {
            finalCommand.append(command).append(" ")
            JSONArray(args).let { argsArray ->
                for (i in 0 until argsArray.length()) {
                    finalCommand.append(argsArray.getString(i))
                    finalCommand.append(" ")
                }
            }
        } else {
            finalCommand.append(command)
        }

        val shell = Compat.createRootShell(
            globalMnt = true,
            devMode = userPrefs.developerMode
        )

        val emitData = fun(name: String, data: String) {
            val jsCode =
                "(function() { try { ${callbackFunc}.${name}.emit('data', ${
                    JSONObject.quote(
                        data
                    )
                }); } catch(e) { console.error('emitData', e); } })();"

            runJs(jsCode)
        }

        val stdout = object : CallbackList<String>(UiThreadHandler::runAndWait) {
            override fun onAddElement(s: String) {
                emitData("stdout", s)
            }
        }

        val stderr = object : CallbackList<String>(UiThreadHandler::runAndWait) {
            override fun onAddElement(s: String) {
                emitData("stderr", s)
            }
        }

        val future = shell.newJob().add(finalCommand.toString()).to(stdout, stderr).enqueue()
        val completableFuture = CompletableFuture.supplyAsync {
            future.get()
        }

        completableFuture.thenAccept { result ->
            val emitExitCode =
                "(function() { try { ${callbackFunc}.emit('exit', ${result.code}); } catch(e) { console.error(`emitExit error: \${e}`); } })();"
            runJs(emitExitCode)


            if (result.code != 0) {
                val emitErrCode =
                    "(function() { try { var err = new Error(); err.exitCode = ${result.code}; err.message = ${
                        JSONObject.quote(
                            result.err.joinToString(
                                "\n"
                            )
                        )
                    };${callbackFunc}.emit('error', err); } catch(e) { console.error('emitErr', e); } })();"
              runJs(emitErrCode)
            }
        }.whenComplete { _, _ ->
            runJsCatching { shell.close() }
        }
    }
}