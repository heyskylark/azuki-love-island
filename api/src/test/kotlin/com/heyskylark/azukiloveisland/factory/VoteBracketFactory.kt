package com.heyskylark.azukiloveisland.factory

import com.heyskylark.azukiloveisland.factory.BracketGroupFactory.Companion.cutBracketGroupsInHalf
import com.heyskylark.azukiloveisland.factory.BracketGroupFactory.Companion.reduceBracketGroup
import com.heyskylark.azukiloveisland.model.voting.BracketGroup
import com.heyskylark.azukiloveisland.model.voting.GenderedVoteBracket
import com.heyskylark.azukiloveisland.model.voting.VoteBracket
import org.bson.types.ObjectId

class VoteBracketFactory {
    companion object {
        const val DEFAULT_GENDERED_VOTE_BRACKET_ID = "test-gendered-vote-bracket"

        fun getListOfGenderedVoteBracketsForUser(
            ip: String,
            twitterHandle: String,
            seasonNumber: Int,
            maleBracketGroups: Set<BracketGroup>,
            femaleBracketGroups: Set<BracketGroup>,
            numOfRounds: Int = 1
        ): List<GenderedVoteBracket> {
            val voteBrackets: MutableList<GenderedVoteBracket> = mutableListOf()
            var currMaleBracketGroups = maleBracketGroups
            var currFemaleBracketGroups = femaleBracketGroups

            for(round in 1..numOfRounds) {
                val newVoteBracket = createGenderedVoteBracket(
                    ip = ip,
                    twitterHandle = twitterHandle,
                    seasonNumber = seasonNumber,
                    bracketNumber = round,
                    maleBracketGroups = currMaleBracketGroups,
                    femaleBracketGroups = currFemaleBracketGroups,
                    finishedVoting = false
                )

                currMaleBracketGroups = newVoteBracket.maleBracketGroups
                currFemaleBracketGroups = newVoteBracket.femaleBracketGroups

                voteBrackets.add(newVoteBracket)
            }

            return voteBrackets
        }

        fun createGenderedVoteBracket(
            ip: String,
            twitterHandle: String,
            seasonNumber: Int,
            bracketNumber: Int,
            maleBracketGroups: Set<BracketGroup>,
            femaleBracketGroups: Set<BracketGroup>,
            finishedVoting: Boolean = false
        ): GenderedVoteBracket {
            return GenderedVoteBracket(
                id = DEFAULT_GENDERED_VOTE_BRACKET_ID,
                ip = ip,
                twitterHandle = twitterHandle,
                seasonNumber = seasonNumber,
                bracketNumber = bracketNumber,
                maleBracketGroups = cutBracketGroupsInHalf(maleBracketGroups),
                femaleBracketGroups = cutBracketGroupsInHalf(femaleBracketGroups),
                finishedVoting = finishedVoting
            )
        }

        fun createVotes(
            seasonNumber: Int,
            bracketNumber: Int,
            initialMaleBracketGroup: Set<BracketGroup>,
            initialFemaleBracketGroup: Set<BracketGroup>,
            numOfVotes: Int,
            finishedVoting: Boolean = false
        ): List<VoteBracket> {
            val reducedMaleBracketGroups = reduceBracketGroup(initialMaleBracketGroup, bracketNumber)
            val reducedFemaleBracketGroups = reduceBracketGroup(initialFemaleBracketGroup, bracketNumber)

            val votes: MutableList<VoteBracket> = mutableListOf()
            for (round in 1..numOfVotes) {
                votes.add(
                    GenderedVoteBracket(
                        id = ObjectId.get().toString(),
                        ip = "0.0.0.$round",
                        twitterHandle = "test-twitter-$round",
                        seasonNumber = seasonNumber,
                        bracketNumber = bracketNumber,
                        maleBracketGroups = reducedMaleBracketGroups,
                        femaleBracketGroups = reducedFemaleBracketGroups,
                        finishedVoting = finishedVoting
                    )
                )
            }

            return votes
        }
    }
}