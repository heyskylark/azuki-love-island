package com.heyskylark.azukiloveisland.factory

import com.heyskylark.azukiloveisland.factory.BracketGroupFactory.Companion.cutBracketGroupsInHalf
import com.heyskylark.azukiloveisland.model.voting.BracketGroup
import com.heyskylark.azukiloveisland.model.voting.GenderedVoteBracket

class VoteBracketFactory {
    companion object {
        const val DEFAULT_GENDERED_VOTE_BRACKET_ID = "test-gendered-vote-bracket"

        fun getListOfGenderedVoteBrackets(
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

            for(round in 0..numOfRounds) {
                val newVoteBracket = createGenderedVoteBracket(
                    ip = ip,
                    twitterHandle = twitterHandle,
                    seasonNumber = seasonNumber,
                    bracketNumber = voteBrackets.size + 1,
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
    }
}