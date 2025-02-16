package com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl

import android.app.Activity
import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dev.dergoogler.mmrl.compat.ext.shareText

@JsonClass(generateAdapter = true)
internal data class Manager(
    val name: String,
    val versionName: String,
    val versionCode: Int,
)

class MMRLInterface(
    val context: Context,
    private val isDark: Boolean,
    private val webview: WebView,
    private val viewModel: WebUIViewModel,
    private val allowedFsApi: Boolean,
    private val allowedKsuApi: Boolean,
) {
    private val activity = context as Activity
    private var windowInsetsController: WindowInsetsControllerCompat =
        WindowCompat.getInsetsController(
            activity.window,
            webview
        )

    init {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    var moshi: Moshi = Moshi.Builder().build()
    private var managerAdapter: JsonAdapter<Manager> = moshi.adapter(Manager::class.java)

    @get:JavascriptInterface
    val manager: String
        get() = managerAdapter.toJson(
            Manager(
                name = viewModel.platform.current,
                versionName = viewModel.versionName,
                versionCode = viewModel.versionCode
            )
        )

    @get:JavascriptInterface
    val mmrl: String
        get() = managerAdapter.toJson(
            Manager(
                name = BuildConfig.APPLICATION_ID,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE
            )
        )

    @get:JavascriptInterface
    val hasAccessToFileSystem: Boolean
        get() = allowedFsApi

    @get:JavascriptInterface
    val hasAccessToAdvancedKernelSuAPI: Boolean
        get() = allowedKsuApi

    @get:JavascriptInterface
    val windowTopInset: Int?
        get() = viewModel.topInset

    @get:JavascriptInterface
    val windowBottomInset: Int?
        get() = viewModel.bottomInset

    @get:JavascriptInterface
    val isLightNavigationBars: Boolean
        get() = windowInsetsController.isAppearanceLightNavigationBars

    @get:JavascriptInterface
    val isDarkMode: Boolean
        get() = isDark

    @JavascriptInterface
    fun setLightNavigationBars(isLight: Boolean) = activity.runOnUiThread {
        windowInsetsController.isAppearanceLightNavigationBars = isLight
    }

    @get:JavascriptInterface
    val isLightStatusBars: Boolean
        get() = windowInsetsController.isAppearanceLightStatusBars

    @JavascriptInterface
    fun setLightStatusBars(isLight: Boolean) = activity.runOnUiThread {
        windowInsetsController.isAppearanceLightStatusBars = isLight
    }

    @get:JavascriptInterface
    val sdk: Int get() = Build.VERSION.SDK_INT

    @JavascriptInterface
    fun shareText(text: String) {
        context.shareText(text)
    }

    @JavascriptInterface
    fun shareText(text: String, type: String) {
        context.shareText(text, type)
    }

    @get:JavascriptInterface
    val recomposeCount get() = viewModel.recomposeCount

    @JavascriptInterface
    fun recompose() = viewModel.recomposeCount++

    private fun logError(message: String) {
        (context as Activity).runOnUiThread {
            webview.loadUrl("javascript:(console.error('$message'))")
        }
    }

    @JavascriptInterface
    fun requestAdvancedKernelSUAPI() {
        if (viewModel.hasRequestedAdvancedKernelSUAPI) {
            logError("WebUI has already requested to access the Advanced KernelSU API and it was rejected")
            return
        }
        if (allowedFsApi) {
            logError("WebUI was already allowed to access the FileSystem API")
            return
        }

        viewModel.dialogRequestAdvancedKernelSUAPI = true
    }

    @JavascriptInterface
    fun requestFileSystemAPI() {
        if (viewModel.hasRequestFileSystemAPI) {
            logError("WebUI has already requested to access the FileSystem API and it was rejected")
            return
        }
        if (allowedFsApi) {
            logError("WebUI was already allowed to access the FileSystem API")
            return
        }

        viewModel.dialogRequestFileSystemAPI = true
    }
}