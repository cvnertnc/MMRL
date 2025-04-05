package com.dergoogler.mmrl.ui.component.dialog

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext

data class ConfirmData(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val onConfirm: () -> Unit,
    val onClose: () -> Unit,
)

@Composable
fun rememberConfirm(): (ConfirmData) -> Unit {
    val context = LocalContext.current
    val theme = MaterialTheme.colorScheme

    val confirm: (ConfirmData) -> Unit = remember {
        { confirm ->
            (context as? Activity)?.addContentView(
                ComposeView(context).apply {
                    setContent {
                        var showDialog by remember { mutableStateOf(true) }

                        if (showDialog) {
                            MaterialTheme(colorScheme = theme) {
                                ConfirmDialog(
                                    onDismissRequest = {
                                        showDialog = false
                                        confirm.onClose()
                                    },
                                    title = confirm.title,
                                    description = confirm.description,
                                    onClose = {
                                        showDialog = false
                                        confirm.onClose()
                                    },
                                    onConfirm = {
                                        showDialog = false
                                        confirm.onConfirm()
                                    }
                                )
                            }
                        }
                    }
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    return confirm
}
