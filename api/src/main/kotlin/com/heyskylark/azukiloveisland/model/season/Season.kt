package com.heyskylark.azukiloveisland.model.season

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Seasons")
data class Season(
    @Id
    val seasonNumber: Int,
    val submissionActive: Boolean = true
)