package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.dto.poap.POAPClaimRequestDto
import com.heyskylark.azukiloveisland.dto.poap.POAPLoadRequestDto
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.service.POAPService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/poaps"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class POAPResource(
    private val poapService: POAPService
) {
    // POAP request dto can just be a large string of newline separated links
    @PostMapping("/seasons/latest/load")
    fun loadPOAPLinksForLatestSeason(
        @RequestBody poapLoadRequestDto: POAPLoadRequestDto
    ): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(poapService.loadPOAPLinksToLatestSeason(poapLoadRequestDto))
    }

    @PostMapping("/seasons/{seasonNumber}/claim")
    fun claimPOAP(
        @PathVariable("seasonNumber") seasonNumber: Int,
        @RequestBody poapClaimRequestDto: POAPClaimRequestDto
    ): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(poapService.claimPOAP(seasonNumber, poapClaimRequestDto))
    }
}
