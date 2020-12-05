package cc.cryptopunks.crypton.feature

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.fragment.app.Fragment
import cc.cryptopunks.crypton.Resolve
import cc.cryptopunks.crypton.Scoped
import cc.cryptopunks.crypton.cliv2.command
import cc.cryptopunks.crypton.context.ActivityResult
import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.PermissionsResult
import cc.cryptopunks.crypton.context.URI
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.fragment.ChatFragmentModule
import cc.cryptopunks.crypton.fragment.FragmentScope

object ShowFileChooser : Scoped<ChatFragmentModule>

internal fun showFileChooser() = feature(

    command(
        name = "select file"
    ) {
        ShowFileChooser
    },

    handler = { _, _: ShowFileChooser ->
        fragment.activity?.let { activity ->
            if (!activity.hasPermissionForReadExternalStorage())
                fragment.requestPermissionForReadExternalStorage()
            else
                fragment.showFileChooser(getContentIntent())
        }
    }
)

private fun Context.hasPermissionForReadExternalStorage(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val result: Int = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }
    return false
}

fun Fragment.requestPermissionForReadExternalStorage() {
    try {
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_STORAGE_PERMISSION_REQUEST_CODE
        )
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

private const val FILE_CHOOSER_REQUEST_CODE = 10001

private const val READ_STORAGE_PERMISSION_REQUEST_CODE = 10002

private fun Fragment.showFileChooser(intent: Intent) = let {
    if (context
            ?.packageManager
            ?.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            ?.size == 0
    )
        Toast.makeText(context, "No file manager present", Toast.LENGTH_LONG).show() else
        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
}

private fun getContentIntent() = Intent(Intent.ACTION_GET_CONTENT).apply {
    type = "*/*"
}

internal fun execUploadResolver(): Resolve = { activityResult ->
    when {
        activityResult !is ActivityResult -> null
        activityResult.resultCode != Activity.RESULT_OK -> null
        activityResult.requestCode != FILE_CHOOSER_REQUEST_CODE -> null
        else -> activityResult.intent.data?.run { Exec.Upload(URI(toString())) }
    }
}

internal fun showFileChooserResolver(): Resolve = { activityResult ->
    when {
        activityResult !is PermissionsResult -> null
        activityResult.requestCode != READ_STORAGE_PERMISSION_REQUEST_CODE -> null
        this !is FragmentScope -> null
        fragment.context?.hasPermissionForReadExternalStorage() != true -> null
        else -> ShowFileChooser
    }
}
