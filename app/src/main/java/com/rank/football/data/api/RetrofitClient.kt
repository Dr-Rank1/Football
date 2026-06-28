package com.rank.football.data.api

import android.content.Context
import com.rank.football.BuildConfig
import com.rank.football.performance.ApiPerformanceEventListenerFactory
import com.rank.football.util.NetworkMonitor
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://v3.football.api-sports.io/"
    private const val CACHE_SIZE = 10L * 1024 * 1024

    @Volatile
    private var apiService: FootballApiService? = null

    @Volatile
    private var appContext: Context? = null

    /** Returns the singleton FootballApiService for the given context. */
    fun getApiService(context: Context): FootballApiService {
        appContext = context.applicationContext
        return apiService ?: synchronized(this) {
            apiService ?: createApiService(context.applicationContext).also { apiService = it }
        }
    }

    private fun createApiService(context: Context): FootballApiService {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, CACHE_SIZE)

        val apiKeyInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-apisports-key", BuildConfig.API_FOOTBALL_KEY)
                .build()
            chain.proceed(request)
        }

        val cacheInterceptor = Interceptor { chain ->
            val online = NetworkMonitor.isOnline(context)
            val cacheControl = if (online) {
                CacheControl.Builder().maxAge(60, TimeUnit.SECONDS).build()
            } else {
                CacheControl.Builder().onlyIfCached().maxStale(3600, TimeUnit.SECONDS).build()
            }
            val request = chain.request().newBuilder()
                .cacheControl(cacheControl)
                .build()
            chain.proceed(request)
        }

        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!NetworkMonitor.isOnline(context)) {
                request = request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder().onlyIfCached().maxStale(3600, TimeUnit.SECONDS).build()
                    )
                    .build()
            }
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val clientBuilder = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(offlineInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .addInterceptor(loggingInterceptor)
            .eventListenerFactory(ApiPerformanceEventListenerFactory(context))

        if (!BuildConfig.DEBUG) {
            clientBuilder.certificatePinner(buildCertificatePinner())
        }

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FootballApiService::class.java)
    }

    /** Builds the release certificate pinner for the football API host. */
    private fun buildCertificatePinner(): CertificatePinner =
        CertificatePinner.Builder()
            .add("v3.football.api-sports.io", "sha256/afwiKY3RxoMmLkuRW1l7QsPZTJPwDS2pdDROQjXw8ig=")
            .add("v3.football.api-sports.io", "sha256/jQJTbIh0grw0/TKTurkEkI/wGk3/BLcW6S1UCQH6FEo=")
            .build()

    /** Creates a plain OkHttp client without API interceptors. */
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}

