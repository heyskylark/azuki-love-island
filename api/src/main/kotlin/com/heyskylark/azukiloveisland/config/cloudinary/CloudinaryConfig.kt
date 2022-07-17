package com.heyskylark.azukiloveisland.config.cloudinary

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudinaryConfig(
    @Value("\${cloudinary.cloud_name}")
    private val cloudName: String,
    @Value("\${cloudinary.api_key}")
    private val apiKey: String,
    @Value("\${cloudinary.api_secret}")
    private val apiSecret: String,
    @Value("\${cloudinary.secure}")
    private val secure: Boolean,
) {
    @Bean("cloudinary")
    fun cloudinary(): Cloudinary {
        return Cloudinary(
            mapOf(
                "cloud_name" to cloudName,
                "api_key" to apiKey,
                "api_secret" to apiSecret,
                "secure" to secure
            )
        )
    }
}