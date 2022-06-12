package com.heyskylark.azukiloveisland.resource

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/health"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class HealthResource {
    @GetMapping
    fun health(): ResponseEntity<String> {
        return ResponseEntity.ok("healthy...")
    }
}
