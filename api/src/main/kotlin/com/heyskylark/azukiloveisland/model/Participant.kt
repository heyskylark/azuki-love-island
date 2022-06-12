package com.heyskylark.azukiloveisland.model

import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import com.heyskylark.azukiloveisland.model.season.Season
import java.net.URL
import java.time.Instant
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Participants")
data class Participant(
    @Id
    val id: String = ObjectId.get().toString(),
    @Indexed(unique = false)
    val azukiId: Long,
    @Indexed(unique = false)
    val ownerAddress: String,
    val imageUrl: URL,
    val backgroundTrait: BackgroundTrait,
    @Indexed(unique = false)
    val twitterHandle: String,
    val seasonNumber: Int,
    val bio: String? = null,
    val hobbies: Set<String>? = null,
    val submitted: Boolean = false,
    val validated: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
