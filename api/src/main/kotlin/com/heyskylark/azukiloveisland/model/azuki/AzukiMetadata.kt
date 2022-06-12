package com.heyskylark.azukiloveisland.model.azuki

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

data class AzukiMetadata(
    val name: String,
    val image: URL,
    val attributes: Set<Trait>
)

data class Trait(
    @JsonProperty("trait_type")
    val traitType: String,
    val value: String
)
