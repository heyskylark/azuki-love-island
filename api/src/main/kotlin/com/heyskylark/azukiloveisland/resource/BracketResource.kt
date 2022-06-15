package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.dto.bracket.BracketCreationRequestDto
import com.heyskylark.azukiloveisland.model.voting.InitialBracket
import com.heyskylark.azukiloveisland.model.voting.BracketType
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.BracketService
import com.heyskylark.azukiloveisland.service.VoteService
import com.heyskylark.azukiloveisland.service.errorcode.BracketErrorCodes
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    @GetMapping("/latest")
    fun getLatestSeasonBracket(): ResponseEntity<String> {
        return ResponseBuilder.buildResponse(bracketService.getLatestSeasonBracket())
    }

    @PostMapping("/generate")
    fun generateLatestSeasonBracket(
        @RequestParam("type", required = false) type: BracketType? = null,
        @RequestBody bracketCreationRequestDto: BracketCreationRequestDto
    ): ResponseEntity<String> {
        val initialBracket = when (type) {
            BracketType.GENDERED,
            null -> bracketService.generateLatestSeasonGenderedBracket(bracketCreationRequestDto)
            else -> ServiceResponse.errorResponse<InitialBracket>(BracketErrorCodes.INVALID_BRACKET_TYPE)
        }

        return ResponseBuilder.buildResponse(initialBracket)
    }
}
