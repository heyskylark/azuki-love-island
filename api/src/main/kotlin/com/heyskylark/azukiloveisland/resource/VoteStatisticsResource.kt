package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.model.azuki.Gender
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.service.VoteStatisticService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/vote"],
    produces = [MediaType.TEXT_PLAIN_VALUE]
)
class VoteStatisticsResource(
    private val voteStatisticService: VoteStatisticService
) {
    @GetMapping("/seasons/{seasonNumber}/genders/{gender}/votes.csv")
    fun getVoteStats(
        @PathVariable("seasonNumber") seasonNumber: Int,
        @PathVariable("gender") gender: Gender,
        @RequestParam("divisions", required = false) divisions: Int? = null
    ): ResponseEntity<String> {
        val csvResponse = voteStatisticService.generateSeasonVotStats(seasonNumber, gender, divisions)

        return if (csvResponse.isSuccess()) {
            val csvValue = csvResponse.getSuccessValue()
                ?: return ResponseEntity.badRequest().body("Generated CSV was not found.")

            ResponseEntity.ok(csvValue)
        } else {
            ResponseBuilder.buildResponse(csvResponse)
        }
    }
}