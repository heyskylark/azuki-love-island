package com.heyskylark.azukiloveisland.model.poap

import java.net.URL
import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "SeasonPoaps")
data class SeasonPOAP(
    @Id
    val seasonNumber: Int,
    val claimStarts: Instant,
    val claimEnds: Instant,
    val claimUrls: Map<String, POAPClaimUrl>,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class POAPClaimUrl(
    val url: URL,
    val claimedBy: String? = null,
    val claimedByIp: String? = null
)
