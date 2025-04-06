package com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.app.moshi
import com.dergoogler.mmrl.utils.file.SuFile
import dev.dergoogler.mmrl.compat.core.MMRLWebUIInterface


class FileInterface(
    webView: WebView,
    context: Context,
) : MMRLWebUIInterface(webView, context) {
    private val file = Compat.fileManager

    @JavascriptInterface
    fun read(path: String): String? =
        runTryJsWith(SuFile(path), "Error while reading from \\'$path\\'.") {
            return@runTryJsWith readText()
        }

    @JavascriptInterface
    fun readBytes(path: String, callbackFunc: String) = runPost {
        SuFile(path).newInputStream().use { input ->
            while (true) {
                val byte = input.read()
                if (byte == -1) break
                val unsignedByte = byte and 0xFF
                runJs("(function() { try { ${callbackFunc}($unsignedByte); } catch(e) { console.error(e); } })();")
            }

            runJs("(function() { try { $callbackFunc(null); } catch(e) { console.error(e); } })();")
        }
    }

    @JavascriptInterface
    fun readBytes(path: String): String =
        moshi.adapter(ByteArray::class.java).toJson(SuFile(path).readBytes()).toString()

    @JavascriptInterface
    fun write(path: String, data: String) =
        runTryJsWith(SuFile(path), "Error while writing to \\'$path\\'") {
            writeText(data)
        }

    @JavascriptInterface
    fun write(path: String, data: Array<Int>) =
        runTryJsWith(SuFile(path), "Error while writing to \\'$path\\'") {
            SuFile(path).writeBytes(ByteArray(data.size) { data[it].toByte() })
        }

    @JavascriptInterface
    fun readAsBase64(path: String): String? =
        runTryJsWith(file, "Error while reading \\'$path\\' as base64") {
            return@runTryJsWith readAsBase64(path)
        }

    @JavascriptInterface
    fun list(path: String): String? = this.list(path, ",")

    @JavascriptInterface
    fun list(path: String, delimiter: String): String? =
        runTryJsWith(SuFile(path), "Error while listing \\'$path\\'") {
            return@runTryJsWith list().joinToString(delimiter)
        }

    @JavascriptInterface
    fun size(path: String): Long = this.size(path, false)

    @JavascriptInterface
    fun size(path: String, recursive: Boolean): Long =
        runTryJsWith(
            SuFile(path),
            "Error while getting size of \\'$path\\'. RECURSIVE: $recursive",
            0L
        ) {
            size(path, recursive)
        }

    @JavascriptInterface
    fun stat(path: String): Long = runTryJsWith(SuFile(path), "Error while stat \\'$path\\'", 0L) {
        return@runTryJsWith stat()
    }

    @JavascriptInterface
    fun stat(path: String, total: Boolean): Long {
        console.error("fs.stat is NOT IMPLEMENTED!")
        return -1
    }

    @JavascriptInterface
    fun delete(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while deleting \\'$path\\'", false) {
            return@runTryJsWith delete()
        }

    @JavascriptInterface
    fun exists(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while checking for existence of \\'$path\\'", false) {
            return@runTryJsWith exists()
        }

    @JavascriptInterface
    fun isDirectory(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while checking if \\'$path\\' is a directory", false) {
            return@runTryJsWith isDirectory
        }

    @JavascriptInterface
    fun isFile(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while checking if \\'$path\\' is a file", false) {
            return@runTryJsWith isFile
        }

    @JavascriptInterface
    fun isSymLink(path: String): Boolean =
        runTryJsWith(
            SuFile(path),
            "Error while checking if \\'$path\\' is a symbolic link",
            false
        ) {
            return@runTryJsWith isSymlink()
        }

    @JavascriptInterface
    fun mkdir(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while creating directory \\'$path\\'", false) {
            return@runTryJsWith mkdir()
        }

    @JavascriptInterface
    fun mkdirs(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while creating directories \\'$path\\'", false) {
            return@runTryJsWith mkdirs()
        }

    @JavascriptInterface
    fun createNewFile(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while creating file \\'$path\\'", false) {
            return@runTryJsWith createNewFile()
        }

    @JavascriptInterface
    fun renameTo(target: String, dest: String): Boolean =
        runTryJsWith(SuFile(target), "Error while renaming \\'$target\\' to \\'$dest\\'", false) {
            return@runTryJsWith renameTo(SuFile(dest))
        }

    @JavascriptInterface
    fun copyTo(path: String, target: String, overwrite: Boolean) =
        runTryJsWith(SuFile(path), "Error while copying \\'$path\\' to \\'$target\\'", false) {
            return@runTryJsWith copyTo(SuFile(target), overwrite)
        }

    @JavascriptInterface
    fun canExecute(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while checking if \\'$path\\' can be executed", false) {
            return@runTryJsWith canExecute()
        }

    @JavascriptInterface
    fun canWrite(path: String): Boolean =
        runTryJsWith(
            SuFile(path),
            "Error while checking if \\'$path\\' can be written to",
            false
        ) {
            return@runTryJsWith canWrite()
        }

    @JavascriptInterface
    fun canRead(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while checking if \\'$path\\' can be read", false) {
            return@runTryJsWith canRead()
        }

    @JavascriptInterface
    fun isHidden(path: String): Boolean =
        runTryJsWith(SuFile(path), "Error while checking if \\'$path\\' is hidden", false) {
            return@runTryJsWith isHidden
        }
}