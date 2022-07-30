package com.heyskylark.azukiloveisland.dto.participant

import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.ParticipantArt
import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import com.heyskylark.azukiloveisland.model.azuki.Gender
import java.net.URL

data class ParticipantWalletAddressesResponseDto(
    val seasonNumber: Int,
    val walletAddresses: Set<String>,
    val newlineSeparatedAddresses: String
)

data class SeasonParticipantsResponseDto(
    val seasonNumber: Int,
    val participants: Set<ParticipantResponseDto>
)

data class ParticipantResponseDto(
    val id: String,
    val azukiId: Long,
    val imageUrl: URL,
    val backgroundTrait: BackgroundTrait,
    val twitterHandle: String,
    val seasonNumber: Int,
    val gender: Gender,
    val quote: String?,
    val bio: String? = null,
    val hobbies: Set<String>? = null,
    val image: ParticipantArt? = null,
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
        quote = participant.quote,
        bio = participant.bio,
        hobbies = participant.hobbies,
        image = participant.image,
        submitted = participant.submitted,
        validated = participant.validated
    )
}