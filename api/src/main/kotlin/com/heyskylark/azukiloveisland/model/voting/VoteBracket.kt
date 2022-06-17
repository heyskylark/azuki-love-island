package com.heyskylark.azukiloveisland.model.voting

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

interface VoteBracket {
    val id: String
    val ip: String
    val twitterHandle: String
    val seasonNumber: Int
    val bracketNumber: Int
    val type: BracketType
    val combinedGroup: Set<BracketGroup>
    val createdAt: Instant
    val updatedAt: Instant
    val finishedVoting: Boolean
}

@Document(collection = "VoteBrackets")
@CompoundIndexes(
    CompoundIndex(name = "seasonNumber_finishedVoting", def = "{'seasonNumber' : -1, 'finishedVoting': 1}"),
    CompoundIndex(name = "seasonNumber_bracketNumber", def = "{'seasonNumber' : -1, 'bracketNumber': 1}")
)
data class GenderedVoteBracket(
    @Id
    override val id: String = ObjectId.get().toString(),
    override val ip: String,
    override val twitterHandle: String,
    override val seasonNumber: Int,
    override val bracketNumber: Int,
    val maleBracketGroups: Set<BracketGroup>,
    val femaleBracketGroups: Set<BracketGroup>,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
    override val finishedVoting: Boolean = false
) : VoteBracket {
    override var type
        get() = BracketType.GENDERED
        set(_) {}
    override val combinedGroup
        @JsonIgnore
        get() = maleBracketGroups + femaleBracketGroups
}
