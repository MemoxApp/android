package cn.memox.ui.provider

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import cn.memox.App
import com.blankj.utilcode.util.ClipboardUtils

class AppUriHandler(private val nav: NavHostController, private val activity: Activity) :
    UriHandler {
    override fun openUri(uri: String) {
        try {
            nav.navigate(uri.toUri())
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            } catch (e: Exception) {
                ClipboardUtils.copyText(uri)
                Toast.makeText(App.CONTEXT, e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}