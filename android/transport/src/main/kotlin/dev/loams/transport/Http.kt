package dev.loams.transport

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

object Http {
    /**
     * One OkHttp client per instance. Redirects are never followed (a redirect could move a
     * bearer token to another origin, §37 §6.4). No read timeout: Watch streams idle between
     * 15 s heartbeats, and the core's 45 s watchdog detects dead ones; unary calls get their
     * deadline from the Connect timeout oracle.
     */
    fun client(trust: TrustPolicy, base: OkHttpClient? = null): OkHttpClient =
        (base?.newBuilder() ?: OkHttpClient.Builder())
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .trust(trust)
            .build()
}
