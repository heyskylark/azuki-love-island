package com.heyskylark.azukiloveisland.dto.poap

import java.net.URL

data class POAPLoadRequestDto(
    val claimStartDateMilli: Long,
    val claimEndDateMilli: Long,
    val poapLinks: String
)

data class POAPClaimRequestDto(
    val twitterHandle: String
)

data class POAPClaimResponseDto(
    val url: URL
)
