package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.dto.vote.VoteRequestDto
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.service.VoteService
import com.heyskylark.azukiloveisland.util.HttpRequestUtil
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/vote"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class VoteResource(
    private val voteService: VoteService
) {
    @GetMapping("/latest")
    fun getLatestBracketForCurrentSeason(): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(voteService.getLatestVoteBracketForLatestSeason())
    }

    @PostMapping
    fun voteOnLatestSeason(@RequestBody voteRequestDto: VoteRequestDto): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(voteService.vote(voteRequestDto))
    }

    @GetMapping("/test")
    fun testIp(): ResponseEntity<String> {
        val ip = HttpRequestUtil.getClientIpAddressIfServletRequestExist()
        return ResponseEntity.ok(ip)
    }
}
