package com.heyskylark.azukiloveisland.dto

import com.heyskylark.azukiloveisland.model.ParticipantArt

data class ParticipantSubmissionDto(
    val azukiId: Long,
    val twitterHandle: String,
    val quote: String,
    val bio: String? = null,
    val hobbies: String? = null, // Comma separated string
    val image: ParticipantArt? = null
)
