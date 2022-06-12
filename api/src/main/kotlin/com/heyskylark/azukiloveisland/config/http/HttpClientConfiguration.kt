package com.heyskylark.azukiloveisland.config.http

import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfiguration {
    @Bean
    fun httpClient(): CloseableHttpClient {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 50000
        connectionManager.validateAfterInactivity = 60
        connectionManager.defaultMaxPerRoute = 50000

        val defaultRequestConfig = RequestConfig.custom()
            .setConnectTimeout(2000)
            .build()

        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(defaultRequestConfig)
            .build()
    }
}
