package cn.memox.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import cn.memox.App
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import okio.FileSystem

@Composable
fun CacheImage(
    src: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = src,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale,
//                            placeholder = painterResource(id = R.drawable.placeholder)
    )
}

val imageLoader = ImageLoader.Builder(App.CONTEXT)
    .crossfade(true)
    .diskCache(
        DiskCache.Builder()
            .maxSizePercent(0.1)
            .minimumMaxSizeBytes(1024)
            .fileSystem(FileSystem.SYSTEM)
            .directory(App.CONTEXT.externalCacheDir ?: App.CONTEXT.cacheDir)
            .build()
    )
    .allowHardware(true)
    .diskCachePolicy(CachePolicy.ENABLED)
    .networkCachePolicy(CachePolicy.ENABLED)
    .memoryCachePolicy(CachePolicy.ENABLED)
    .memoryCache(
        MemoryCache.Builder(App.CONTEXT)
            .strongReferencesEnabled(true)
            .weakReferencesEnabled(true)
            .build()
    )
    .build()