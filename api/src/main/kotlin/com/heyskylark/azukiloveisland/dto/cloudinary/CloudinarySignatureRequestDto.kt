package com.heyskylark.azukiloveisland.dto.cloudinary

data class CloudinarySignatureRequestDto(
    val fileSize: Long,
    val timestamp: Long,
    val transformations: String? = null
)
