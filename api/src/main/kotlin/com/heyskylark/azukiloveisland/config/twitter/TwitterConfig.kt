package com.heyskylark.azukiloveisland.config.twitter

import com.twitter.clientlib.TwitterCredentialsBearer
import com.twitter.clientlib.api.TwitterApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TwitterConfig(
    @Value("\${twitter.api_key}")
    private val apiKey: String,
    @Value("\${twitter.api_secret}")
    private val apiSecret: String,
    @Value("\${twitter.bearer_token}")
    private val bearerToken: String,
) {
    @Bean("twitterApi")
    fun twitterApi(): TwitterApi {
        val credentials = TwitterCredentialsBearer(bearerToken)
        val apiInstance = TwitterApi()
        apiInstance.setTwitterCredentials(credentials)

        return apiInstance
    }
}