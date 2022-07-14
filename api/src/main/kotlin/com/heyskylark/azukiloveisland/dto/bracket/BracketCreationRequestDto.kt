package com.heyskylark.azukiloveisland.dto.bracket

data class BracketCreationRequestDto(
    val voteStartDateMilli: Long,
    val voteDeadlineMilli: Long,
    val voteGapTimeMilliseconds: Long
)
