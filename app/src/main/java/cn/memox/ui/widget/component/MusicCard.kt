package cn.memox.ui.widget.component

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import cn.memox.R
import cn.memox.ui.theme.Gap
import cn.memox.ui.theme.ImageSize
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.green2
import cn.memox.ui.theme.green3
import cn.memox.ui.widget.CacheImage
import cn.memox.ui.widget.LoadingBar
import cn.memox.ui.widget.Style
import cn.memox.ui.widget.component.music.CacheManager
import cn.memox.ui.widget.component.music.MusicCache
import cn.memox.ui.widget.component.music.MusicWebViewProvider
import cn.memox.ui.widget.imageLoader
import cn.memox.ui.widget.key
import cn.memox.utils.duration
import cn.memox.utils.keep
import cn.memox.utils.openNetease
import cn.memox.utils.string
import cn.memox.utils.width
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

object MusicType {
    const val netease = "163"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicCard(modifier: Modifier = Modifier, type: String, id: String) {
    val ctx = LocalContext.current
    val cover = keep(v = "")
    val audio = keep(v = "")
    val title = keep(v = "")
    val progress = keep(v = 0)
    var onDrag by keep(false)
    val duration = keep(v = -1)
    val scope = rememberCoroutineScope()
    val defaultMuteColor = keep(v = green2)
    val defaultActiveColor = keep(v = green3)
    val mp = remember {
        MediaPlayer()
    }
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(type, id) {
        val cache = CacheManager[type, id]
        if (cache != null) {
            cover.value = cache.cover.orEmpty()
            title.value = cache.title.orEmpty()
        }
        val url = when (type) {
            MusicType.netease -> "https://music.163.com/outchain/player?type=2&id=${id}&auto=1&height=66"
            else -> ""
        }

        fun trySave() {
            if (cover.value.isNotBlank() && title.value.isNotBlank()) {
                val c = MusicCache(cover.value, title.value)
                CacheManager.put(type, id, c)
            }
        }
        MusicWebViewProvider(ctx, onCover = {
            if (!(it.contains("default") || it == cover.value)) {
                Log.i("Cover", "$it\n${cover.value}")
                cover.value = it
                trySave()
            }
        }, onAudio = {
            audio.value = it
            trySave()
        }, onTitle = {
            if (it.isNotBlank() && it != "null" && !it.contains("undefined")) {
                title.value = it
            }
            trySave()
        }).web.apply { loadUrl(url) }
    }
    Box(modifier = Modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            onDrag = true
                        },
                        onHorizontalDrag = { _, offset ->
                            val d = max(duration.value, 0)
                            val fraction = offset / width()
                            val offsetDuration = d * fraction
                            progress.value += offsetDuration.toInt()
                            progress.value = progress.value.coerceIn(0, d)
                            Log.i("MusicCard", "${progress.value.duration} / ${mp.currentPosition}")
                            if (abs(progress.value - mp.currentPosition) < 500) {
                                progress.value = mp.currentPosition
                            }
                        },
                        onDragCancel = {
                            onDrag = false
                        },
                        onDragEnd = {
                            onDrag = false
                            if (abs(progress.value - mp.currentPosition) > 500) {
                                mp.seekTo(progress.value)
                            }
                        }
                    )
                }
                .background(if (cover.value.isNotBlank()) defaultMuteColor.value else colors.card)
                .drawBehind {
                    val fraction = progress.value.toFloat() / max(duration.value, 1)
                    drawRoundRect(
                        defaultActiveColor.value,
                        size = size.copy(width = size.width * fraction),
                    )
                }
                .padding(Gap.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cover.value.isBlank() || title.value.isBlank()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LoadingBar(Modifier.padding(vertical = Gap.Mid))
                }
            } else {
                DisposableEffect(audio.value) {
                    val oldAudio = audio.value
                    if (oldAudio.isNotBlank()) {
                        mp.setDataSource(ctx, oldAudio.toUri())
                        mp.prepareAsync()
                        mp.setOnPreparedListener {
                            scope.launch {
                                while (true) {
                                    delay(100)
                                    duration.value = mp.duration
                                    if (!onDrag)
                                        progress.value = mp.currentPosition
                                }
                            }
                        }
                    }
                    onDispose {
                        if (oldAudio.isNotBlank()) {
                            Log.i("MusicCard", "MP Released")
                            mp.release()
                        }
                    }
                }
                DisposableEffect(cover.value) {
                    val req = ImageRequest.Builder(ctx)
                        .data(cover.value)
                        .allowHardware(false)
                        .diskCacheKey(cover.value.key)
                        .memoryCacheKey(cover.value.key)
                        .build()
                    val cancel = scope.launch {
                        val result = imageLoader.execute(req).drawable ?: return@launch
                        val bitmap = result.toBitmapOrNull(128, 128) ?: return@launch
                        val palette = Palette.from(bitmap).generate()
                        defaultActiveColor.value = Color(palette.getVibrantColor(green3.toArgb()))
                        defaultMuteColor.value = Color(palette.getMutedColor(green2.toArgb()))
                    }
                    onDispose {
                        cancel.cancel()
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (mp.isPlaying) {
                                mp.pause()
                            } else {
                                mp.start()
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    CacheImage(
                        src = cover.value, contentDescription = "",
                        style = Style.Original,
                        modifier = Modifier
                            .size(ImageSize.Large * 2)
                    )
                    Icon(
                        painter = painterResource(id = if (mp.isPlaying) R.drawable.pause_fill else R.drawable.play_fill),
                        contentDescription = "",
                        modifier = Modifier
                            .size(ImageSize.Big),
                        tint = Color.White
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = Gap.Mid)
                ) {
                    Text(
                        text = title.value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.W900,
                        color = Color.White,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Text(
                        text = "${progress.value.duration} / ${duration.value.duration}",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.W900
                    )
                }
            }
        }
        Row(
            Modifier
                .padding(horizontal = Gap.Mid, vertical = Gap.Mid)
                .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .clickable {
                    uriHandler.openNetease(id)
                }
                .padding(vertical = Gap.Small, horizontal = Gap.Small)
                .padding(start = Gap.Tiny)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(Gap.Tiny)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.netease_cloud_music),
                contentDescription = string(R.string.open_netease_music),
                tint = Color.White,
                modifier = Modifier.size(ImageSize.XSmall)
            )
            Icon(
                painter = painterResource(id = R.drawable.arrow_right),
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier.size(ImageSize.XSmall)
            )
        }
    }
}

@Preview
@Composable
fun MusicCardPreview() {
    MusicCard(type = MusicType.netease, id = "28287132")
}