package com.heyskylark.azukiloveisland.model.voting

import com.fasterxml.jackson.annotation.JsonIgnore
import com.heyskylark.azukiloveisland.model.Participant

data class BracketGroup(
    val submissionId1: String,
    val submissionId2: String?,
    val sortOrder: Int
) {
    constructor(winningResults: WinningResultsBracketGroup) : this(
        submissionId1 = winningResults.submissionId1,
        submissionId2 = winningResults.submissionId2,
        sortOrder = winningResults.sortOrder
    )
}

data class ParsedWinningResultsBracketGroup(
    val submission1: Participant,
    val sub1VoteCount: Int = 0,
    val submission2: Participant?,
    val sub2VoteCount: Int = 0,
    val sortOrder: Int
)

data class WinningResultsBracketGroup(
    val submissionId1: String,
    @JsonIgnore
    val sub1VoteCount: Int = 0,
    val submissionId2: String?,
    @JsonIgnore
    val sub2VoteCount: Int = 0,
    val sortOrder: Int
)
