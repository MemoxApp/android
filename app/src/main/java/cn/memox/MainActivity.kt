package cn.memox

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.view.WindowCompat
import cn.memox.ui.markdown.WebViewProvider
import cn.memox.ui.provider.AppUriHandler
import cn.memox.ui.theme.MemoxTheme
import cn.memox.ui.viewer.ImageViewerManger
import cn.memox.utils.save
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

val LocalPicker = compositionLocalOf<ActivityResultLauncher<MainActivity.ChooseFiles>> {
    error("No LocalPicker provided")
}
val LocalWebView = staticCompositionLocalOf<WebView> {
    error("No WebView provided")
}
val LocalImageViewer = staticCompositionLocalOf<ImageViewerManger> {
    error("No WebView provided")
}

class MainActivity : ComponentActivity() {
    private val getContents = registerForActivityResult(GetFiles()) {}

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val nav = rememberAnimatedNavController()
            val imageViewerManger = ImageViewerManger()
            val uriHandler = AppUriHandler(nav, this)
            CompositionLocalProvider(
                LocalPicker provides getContents,
                LocalActivity provides this,
                LocalNav provides nav,
                LocalUriHandler provides uriHandler,
                LocalWebView provides WebViewProvider(this, imageViewerManger, uriHandler).web,
                LocalImageViewer provides imageViewerManger,
            ) {
                MemoxTheme {
                    AppNav(nav)
                }
            }
        }
    }


    private val LocalActivity = compositionLocalOf<Activity> {
        this
    }

    @Composable
    fun activity() = LocalActivity.current


    data class ChooseFiles(val input: String = "image/*", val callback: (List<String>) -> Unit)

    /**
     * Get files
     *
     * @constructor Create empty Get images
     */
    open class GetFiles :
        ActivityResultContract<ChooseFiles, List<@JvmSuppressWildcards Uri>>() {
        var input = ChooseFiles {}

        @CallSuper
        override fun createIntent(context: Context, input: ChooseFiles): Intent {
            this.input = input
            return Intent(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(input.input)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        final override fun getSynchronousResult(
            context: Context,
            input: ChooseFiles
        ): SynchronousResult<List<Uri>>? = null

        final override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
            val result = intent.takeIf {
                resultCode == Activity.RESULT_OK
            }?.getClipDataUris() ?: emptyList()
            if (result.isNotEmpty()) {
                val res = result.mapNotNull {
                    it.save("tmp")
                }
                if (res.isNotEmpty()) {
                    input.callback(res)
                }
            }
            return result
        }

        internal companion object {
            internal fun Intent.getClipDataUris(): List<Uri> {
                // Use a LinkedHashSet to maintain any ordering that may be
                // present in the ClipData
                val resultSet = LinkedHashSet<Uri>()
                data?.let { data ->
                    resultSet.add(data)
                }
                val clipData = clipData
                if (clipData == null && resultSet.isEmpty()) {
                    return emptyList()
                } else if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        if (uri != null) {
                            resultSet.add(uri)
                        }
                    }
                }
                return ArrayList(resultSet)
            }
        }
    }
}