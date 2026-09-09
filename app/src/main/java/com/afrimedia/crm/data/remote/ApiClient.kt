package com.afrimedia.crm.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds a Retrofit client pointed at <site>/wp-json/amcrm/v1/ using the
 * credentials in SessionManager. Rebuilt whenever those credentials change
 * (e.g. after logging in, or logging out and into a different site), since
 * Retrofit's base URL and auth header are fixed at construction time.
 */
object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private var cachedService: ApiService? = null
    private var cachedKey: String? = null

    fun service(session: SessionManager): ApiService {
        val key = "${session.siteUrl}|${session.username}|${session.appPassword}"
        cachedService?.let { if (cachedKey == key) return it }

        val authInterceptor = Interceptor { chain ->
            val credential = Credentials.basic(session.username, session.appPassword)
            val request = chain.request().newBuilder()
                .header("Authorization", credential)
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val contentType = "application/json".toMediaType()
        val base = session.siteUrl.trimEnd('/') + "/wp-json/amcrm/v1/"

        val retrofit = Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        val service = retrofit.create(ApiService::class.java)
        cachedService = service
        cachedKey = key
        return service
    }

    /** Builds a one-off client for the login screen's "test connection" call, before credentials are saved. */
    fun probe(siteUrl: String, username: String, appPassword: String): ApiService {
        val authInterceptor = Interceptor { chain ->
            val credential = Credentials.basic(username, appPassword)
            val request = chain.request().newBuilder()
                .header("Authorization", credential)
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        val contentType = "application/json".toMediaType()
        val base = siteUrl.trimEnd('/') + "/wp-json/amcrm/v1/"
        val retrofit = Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
