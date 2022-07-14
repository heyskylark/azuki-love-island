package com.heyskylark.azukiloveisland.model

import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import com.heyskylark.azukiloveisland.model.azuki.Gender
import com.heyskylark.azukiloveisland.model.season.Season
import java.net.URL
import java.time.Instant
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Participants")
@CompoundIndexes(
    CompoundIndex(name = "seasonNumber_validated", def = "{'seasonNumber' : -1, 'validated': 1}")
)
data class Participant(
    @Id
    val id: String = ObjectId.get().toString(),
    @Indexed
    val azukiId: Long,
    @Indexed
    val ownerAddress: String,
    val imageUrl: URL,
    val backgroundTrait: BackgroundTrait,
    @Indexed
    val twitterHandle: String,
    val seasonNumber: Int,
    val quote: String? = null,
    val bio: String? = null,
    val hobbies: Set<String>? = null,
    val submitted: Boolean = false,
    val validated: Boolean = false,
    val gender: Gender = Gender.UNDETERMINED,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
