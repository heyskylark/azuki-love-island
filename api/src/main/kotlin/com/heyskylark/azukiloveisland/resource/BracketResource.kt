package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.dto.VoteDto
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.service.BracketService
import com.heyskylark.azukiloveisland.service.VoteService
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
    value = ["/brackets"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class BracketResource(
    private val bracketService: BracketService,
    private val voteService: VoteService
) {
//    @GetMapping("/seasons/{seasonNumber}")
//    fun getSeasonBrackets(@PathVariable("seasonNumber") seasonNumber: Int): ResponseEntity<String> {
//        return ResponseBuilder.buildResponse(bracketService.getSeasonBrackets(seasonNumber))
//    }
//
//    @GetMapping("/{bracketId}")
//    fun getBracket(@PathVariable("bracketId") bracketId: String): ResponseEntity<String> {
//        return ResponseBuilder.buildResponse(bracketService.getBracket(bracketId))
//    }
//
//    @PostMapping("/{bracketId}/groups/{groupNumber}/vote")
//    fun vote(
//        @PathVariable("bracketId") bracketId: String,
//        @PathVariable("groupNumber") groupNumber: Int,
//        @RequestBody voteDto: VoteDto
//    ): ResponseEntity<String> {
//        return ResponseBuilder.buildResponse(
//                voteService.vote(
//                bracketId = bracketId,
//                groupNumber = groupNumber,
//                voteDto = voteDto
//            )
//        )
//    }
//
//    @GetMapping("/{bracketId}/users/{userId}")
//    fun getBracketVotes(
//        @PathVariable("bracketId") bracketId: String,
//        @PathVariable("userId") userId: String
//    ): ResponseEntity<String> {
//        return ResponseBuilder.buildResponse(voteService.getVotes(bracketId, userId))
//    }
}