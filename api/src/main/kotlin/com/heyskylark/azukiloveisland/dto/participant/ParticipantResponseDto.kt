package com.heyskylark.azukiloveisland.dto.participant

import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import com.heyskylark.azukiloveisland.model.azuki.Gender
import java.net.URL

data class ParticipantResponseDto(
    val id: String,
    val azukiId: Long,
    val imageUrl: URL,
    val backgroundTrait: BackgroundTrait,
    val twitterHandle: String,
    val seasonNumber: Int,
    val gender: Gender,
    val bio: String? = null,
    val hobbies: Set<String>? = null,
    val submitted: Boolean = false,
    val validated: Boolean = false,
) {
    constructor(participant: Participant) : this (
        id = participant.id,
        azukiId = participant.azukiId,
        imageUrl = participant.imageUrl,
        backgroundTrait = participant.backgroundTrait,
        twitterHandle = participant.twitterHandle,
        seasonNumber = participant.seasonNumber,
        gender = participant.gender,
        bio = if (true || participant.validated) participant.bio else null,
        hobbies = if (true || participant.validated) participant.hobbies else null,
        submitted = participant.submitted,
        validated = participant.validated
    )
}