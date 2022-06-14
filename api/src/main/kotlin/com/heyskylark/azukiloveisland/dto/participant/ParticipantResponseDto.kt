package com.heyskylark.azukiloveisland.dto.participant

import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import java.net.URL

data class ParticipantResponseDto(
    val azukiId: Long,
    val ownerAddress: String,
    val imageUrl: URL,
    val backgroundTrait: BackgroundTrait,
    val twitterHandle: String,
    val seasonNumber: Int,
    val bio: String? = null,
    val hobbies: Set<String>? = null,
    val submitted: Boolean = false,
    val validated: Boolean = false,
) {
    constructor(participant: Participant) : this (
        azukiId = participant.azukiId,
        ownerAddress = participant.ownerAddress,
        imageUrl = participant.imageUrl,
        backgroundTrait = participant.backgroundTrait,
        twitterHandle = participant.twitterHandle,
        seasonNumber = participant.seasonNumber,
        bio = if (true || participant.validated) participant.bio else null,
        hobbies = if (true || participant.validated) participant.hobbies else null,
        submitted = participant.submitted,
        validated = participant.validated
    )
}