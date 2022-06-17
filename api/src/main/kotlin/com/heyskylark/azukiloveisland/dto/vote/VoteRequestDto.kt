package com.heyskylark.azukiloveisland.dto.vote

import com.heyskylark.azukiloveisland.model.voting.BracketGroup

data class VoteRequestDto(
    val twitterHandle: String,
    val maleBracketGroups: Set<BracketGroup>,
    val femaleBracketGroups: Set<BracketGroup>
)
