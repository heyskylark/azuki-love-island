package com.heyskylark.azukiloveisland.dto.vote

import com.fasterxml.jackson.annotation.JsonIgnore
import com.heyskylark.azukiloveisland.model.voting.ParsedWinningResultsBracketGroup
import com.heyskylark.azukiloveisland.model.voting.WinningResultsBracketGroup

data class GenderedVoteRoundsResponse(
    val seasonNumber: Int,
    val rounds: List<GenderedRoundWinners> = emptyList()
)

data class ParsedGenderedRoundWinners(
    val maleWinners: List<ParsedWinningResultsBracketGroup>,
    val femaleWinners: List<ParsedWinningResultsBracketGroup>,
    val roundNumber: Int
)

data class GenderedRoundWinners(
    val maleWinners: List<WinningResultsBracketGroup>,
    val femaleWinners: List<WinningResultsBracketGroup>,
    val roundNumber: Int
) {
    val combinedGroup
        @JsonIgnore
        get() = maleWinners + femaleWinners
}
