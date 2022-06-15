package com.heyskylark.azukiloveisland.dao

import com.heyskylark.azukiloveisland.model.Participant
import java.util.stream.Stream
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("participantDao")
interface ParticipantDao : CrudRepository<Participant, String> {
    @Query("{'seasonNumber' : { '\$eq' : ?0 }}")
    fun findBySeasonNumber(seasonNumber: Int): Set<Participant>

    fun findBySeasonNumberAndValidated(seasonNumber: Int, validated: Boolean): Set<Participant>

    fun findByAzukiIdAndSeasonNumber(azukiId: Long, seasonNumber: Int): Participant?

    fun findByOwnerAddressAndSeasonNumber(ownerAddress: String, seasonNumber: Int): Participant?

    fun findByTwitterHandleAndSeasonNumber(twitterHandle: String, seasonNumber: Int): Participant?
}
