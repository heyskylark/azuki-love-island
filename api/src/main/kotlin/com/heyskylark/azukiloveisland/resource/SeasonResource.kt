package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.service.SeasonService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/seasons"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class SeasonResource(
    private val seasonService: SeasonService
) {
    @GetMapping("/latest")
    fun getLatestSeason(): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(seasonService.getLatestSeason())
    }
}