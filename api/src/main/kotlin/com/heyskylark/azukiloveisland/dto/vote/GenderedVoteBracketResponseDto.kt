package com.heyskylark.azukiloveisland.dto.vote

import com.heyskylark.azukiloveisland.model.voting.BracketGroup
import com.heyskylark.azukiloveisland.model.voting.BracketType

data class GenderedVoteBracketResponseDto(
    val twitterHandle: String?,
    val seasonNumber: Int,
    val roundNumber: Int,
    val maleBracketGroups: Set<BracketGroup>,
    val femaleBracketGroups: Set<BracketGroup>,
    val hasVoted: Boolean,
    val finishedVoting: Boolean,
    val finalRound: Boolean,
    val canClaimPOAP: Boolean
) {
    val type: BracketType = BracketType.GENDERED
}
