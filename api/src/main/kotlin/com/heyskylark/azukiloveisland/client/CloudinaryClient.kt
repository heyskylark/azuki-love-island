package com.heyskylark.azukiloveisland.client

import com.cloudinary.Cloudinary
import java.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component("cloudinaryClient")
class CloudinaryClient(
    @Value("\${cloudinary.api_secret}")
    private val apiSecret: String,
    private val cloudinary: Cloudinary
) {
    fun retrieveSignature(
        seasonNumber: Int,
        timestampSeconds: Long,
        transformations: String? = null
    ): String {
        val paramsToSign = mapOf("timestamp" to timestampSeconds)
        transformations?.let { paramsToSign.plus("transformation" to it) }

        return cloudinary.apiSignRequest(paramsToSign, apiSecret)
    }
}