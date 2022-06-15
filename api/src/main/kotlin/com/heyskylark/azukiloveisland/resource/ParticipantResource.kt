package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.dto.ParticipantSubmissionDto
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.service.ParticipantService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/participants"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class ParticipantResource(
    private val participantService: ParticipantService
) {
    @GetMapping("seasons/latest/count")
    fun getLatestSeasonParticipantCount(): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.getLatestSeasonSubmissionCount())
    }

    @GetMapping("seasons/latest")
    fun getLatestSeasonParticipants(): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.geLatestSeasonSubmissions())
    }

    @GetMapping("seasons/{seasonNumber}/count")
    fun getParticipantCount(@PathVariable("seasonNumber") seasonNumber: Int): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.getSubmissionCount(seasonNumber))
    }

    @GetMapping("seasons/{seasonNumber}")
    fun getSeasonParticipants(@PathVariable("seasonNumber") seasonNumber: Int): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.getSeasonSubmissions(seasonNumber))
    }

    @GetMapping("/{participantId}")
    fun getParticipant(@PathVariable("participantId") participantId: String): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.getParticipant(participantId))
    }

    @PostMapping
    fun submitParticipantToLatestSeason(
        @RequestBody participantSubmissionDto: ParticipantSubmissionDto
    ): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.submitParticipant(participantSubmissionDto))
    }
}
