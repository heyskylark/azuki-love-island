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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/participants"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class ParticipantResource(
    private val participantService: ParticipantService
) {
    /* Contestants */
    @GetMapping("seasons/latest")
    fun getLatestSeasonContestants(
        @RequestParam("filter", required = false) filter: String?
    ): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.getLatestSeasonContestants())
    }

    @GetMapping("seasons/{seasonNumber}")
    fun getSeasonContestants(@PathVariable("seasonNumber") seasonNumber: Int): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(
            participantService.getSeasonContestants(seasonNumber)
        )
    }

    /* Submissions */
    @GetMapping("seasons/{seasonNumber}/submissions")
    fun getSeasonSubmissions(
        @PathVariable("seasonNumber") seasonNumber: Int,
        @RequestParam("submitted", required = false) submitted: Boolean?
    ): ResponseEntity<String> {
        return ResponseEntity.notFound().build()
        // return ResponseBuilder.buildResponse(participantService.getSeasonSubmissions(seasonNumber))
    }

    @GetMapping("seasons/latest/submissions/count")
    fun getLatestSeasonSubmissionCount(): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.getLatestSeasonSubmissionCount())
    }

    @GetMapping("seasons/{seasonNumber}/submissions/count")
    fun getParticipantCount(@PathVariable("seasonNumber") seasonNumber: Int): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(participantService.getSubmissionCount(seasonNumber))
    }

    /* Participants */
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
