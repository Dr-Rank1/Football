package com.rank.football.performance

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import okhttp3.OkHttpClient

/** Tunes Coil image loading for football logos and thumbnails. */
object ImagePipelineOptimizer {

    /** Builds a Coil ImageLoader tuned for GoalStream asset patterns. */
    fun createOptimizedLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.03)
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder().build()
            }
            .respectCacheHeaders(false)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }

    /** Clears memory and disk image caches. */
    fun clearCaches(loader: ImageLoader) {
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
    }
}
