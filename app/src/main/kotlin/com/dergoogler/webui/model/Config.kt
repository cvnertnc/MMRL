package com.dergoogler.webui.model

import com.dergoogler.webui.webUiConfig
import com.squareup.moshi.JsonClass

object WebUIPermissions {
    const val PLUGIN_DEX_LOADER = "webui.permission.PLUGIN_DEX_LOADER"
    const val DSL_DEX_LOADING = "webui.permission.DSL_DEX_LOADING"
}

@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersion(
    val required: Int = 1,
    val supportText: String? = null,
    val supportLink: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfigRequire(
    val version: WebUIConfigRequireVersion = WebUIConfigRequireVersion(),
)

@JsonClass(generateAdapter = true)
data class WebUIConfigDsl(
    val path: String? = null,
    val className: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfig(
    val dsl: WebUIConfigDsl = WebUIConfigDsl(),
    val plugins: List<String> = emptyList(),
    val require: WebUIConfigRequire = WebUIConfigRequire(),
    val permissions: List<String> = emptyList(),
    val historyFallback: Boolean = false,
    val title: String? = null,
    val icon: String? = null,
    val windowResize: Boolean = true,
    val backHandler: Boolean = true,
    val exitConfirm: Boolean = true,
    val historyFallbackFile: String = "index.html",
) {
    val hasPluginDexLoaderPermission = permissions.contains(WebUIPermissions.PLUGIN_DEX_LOADER)
    val hasDslDexLoadingPermission = permissions.contains(WebUIPermissions.DSL_DEX_LOADING)


    companion object {
        fun String.toWebUiConfig() = webUiConfig(this)
    }
}
