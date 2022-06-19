package com.heyskylark.azukiloveisland.dto.vote

import com.heyskylark.azukiloveisland.model.voting.BracketGroup
import com.heyskylark.azukiloveisland.model.voting.BracketType
import com.heyskylark.azukiloveisland.model.voting.GenderedVoteBracket

data class GenderedVoteBracketResponseDto(
    val twitterHandle: String,
    val seasonNumber: Int,
    val bracketNumber: Int,
    val maleBracketGroups: Set<BracketGroup>,
    val femaleBracketGroups: Set<BracketGroup>,
    val finishedVoting: Boolean,
) {
    val type: BracketType = BracketType.GENDERED

    constructor(voteBracket: GenderedVoteBracket) : this(
        twitterHandle = voteBracket.twitterHandle,
        seasonNumber = voteBracket.seasonNumber,
        bracketNumber = voteBracket.bracketNumber,
        maleBracketGroups = voteBracket.maleBracketGroups,
        femaleBracketGroups = voteBracket.femaleBracketGroups,
        finishedVoting = voteBracket.finishedVoting
    )
}
