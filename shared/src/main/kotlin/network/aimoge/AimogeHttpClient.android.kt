package com.valoser.futacha.shared.network.aimoge

import com.valoser.futacha.shared.network.MainThreadSafeResponseCloseInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * The client for あいもげ only: no cookies, no redirects followed (a 3xx is a failure), no replayed requests. The
 * Futaba client keeps its cookies and is never used for あいもげ.
 */
fun createAimogeHttpClient(appVersion: String): HttpClient = HttpClient(OkHttp) {
    configureAimogeCommon(appVersion)
    engine {
        config {
            // Same four guards as the Futaba client: no main-thread TLS close, no redirects, and no silent replay of a
            // buffered POST (a reply, presign or self-delete must never go twice).
            addInterceptor(MainThreadSafeResponseCloseInterceptor)
            followRedirects(false)
            followSslRedirects(false)
            retryOnConnectionFailure(false)
        }
    }
}
