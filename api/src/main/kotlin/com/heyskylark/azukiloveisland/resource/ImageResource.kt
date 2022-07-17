package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.dto.cloudinary.CloudinarySignatureRequestDto
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.service.CloudinaryService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/images"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class ImageResource(
    private val cloudinaryService: CloudinaryService
) {
    @PostMapping("signature")
    fun getCloudinarySignature(
        @RequestBody request: CloudinarySignatureRequestDto
    ): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(
            cloudinaryService.generateCloudinarySignature(
                fileSize = request.fileSize,
                timestampSeconds = request.timestamp,
                transformations = request.transformations
            )
        )
    }
}