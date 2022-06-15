package com.heyskylark.azukiloveisland.model.voting

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

interface InitialBracket {
    val id: String
    val seasonNumber: Int
    val type: BracketType
    val voteDeadline: Instant
    val combinedGroups: Set<BracketGroup>
    val createdAt: Instant
    val updatedAt: Instant

    fun numberOfBrackets(): Int
}

@Document(collection = "InitialBrackets")
data class GenderedInitialBracket(
    @Id
    override val id: String = ObjectId.get().toString(),
    @Indexed(unique = true)
    override val seasonNumber: Int,
    override val voteDeadline: Instant,
    val maleBracketGroups: Set<BracketGroup>,
    val femaleBracketGroups: Set<BracketGroup>,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : InitialBracket {
    override val type = BracketType.GENDERED
    override val combinedGroups: Set<BracketGroup>
        @JsonIgnore
        get() = maleBracketGroups + femaleBracketGroups

    override fun numberOfBrackets(): Int {
        var groupCount = maleBracketGroups.size
        var numOfBrackets = 1
        var loops = 0

        while (groupCount > 4) {
            groupCount /= 2

            if (loops > 10) {
                throw RuntimeException("Somehow get caught in a loop, brackets will never get this big...")
            }
            loops++
            numOfBrackets++
        }

        return numOfBrackets
    }
}
