package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.client.CloudinaryClient
import com.heyskylark.azukiloveisland.dto.cloudinary.CloudinarySignatureRequestDto
import com.heyskylark.azukiloveisland.dto.cloudinary.CloudinarySignatureResponseDto
import com.heyskylark.azukiloveisland.serialization.ErrorResponse
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.CloudinaryErrorCodes
import org.springframework.stereotype.Component

@Component("cloudinaryService")
class CloudinaryService(
    private val seasonService: SeasonService,
    private val cloudinaryClient: CloudinaryClient
) {
    companion object {
        private const val ONE_KB = 1000
        private const val ONE_MB = ONE_KB * 1000
        private const val MAX_FILE_SIZE_BYTES = ONE_MB * 10
    }
    fun generateCloudinarySignature(
        fileSize: Long,
        timestampSeconds: Long,
        transformations: String? = null
    ): ServiceResponse<CloudinarySignatureResponseDto> {
        return validateImageSignature(fileSize) ?: run {
            val latestSeasonResponse = seasonService.getLatestSeason()
            val latestSeason = latestSeasonResponse.getSuccessValue() ?: run {
                val errorResponse = latestSeasonResponse as ErrorResponse

                return ServiceResponse.errorResponse(errorResponse.errorCode)
            }

            ServiceResponse.successResponse(
                CloudinarySignatureResponseDto(
                    cloudinaryClient.retrieveSignature(latestSeason.seasonNumber, timestampSeconds, transformations)
                )
            )
        }
    }

    private fun validateImageSignature(fileSize: Long): ServiceResponse<CloudinarySignatureResponseDto>? {
        fileSize.takeIf { it > MAX_FILE_SIZE_BYTES }?.let {
            return ServiceResponse.errorResponse(CloudinaryErrorCodes.IMAGE_FILE_TOO_LARGE)
        }

        return null
    }
}