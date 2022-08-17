package com.heyskylark.azukiloveisland.dto.vote

import com.heyskylark.azukiloveisland.model.Participant

data class GenderedVoteCountResults(
    val femaleVoteCount: List<VoteCountResults>,
    val maleVoteCount: List<VoteCountResults>
)

data class VoteCountResults(
    val twitterHandle: String,
    val azukiId: Long,
    val voteCount: Int
)
