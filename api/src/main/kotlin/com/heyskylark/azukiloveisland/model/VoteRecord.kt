package com.heyskylark.azukiloveisland.model

data class VoteRecord(
    val fromUseId: String,
    val toGroupId: String,
    val participantVotedId: Long
)
