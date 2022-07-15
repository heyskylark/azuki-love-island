package com.heyskylark.azukiloveisland.factory

import com.heyskylark.azukiloveisland.factory.BracketGroupFactory.Companion.createBracketGroups
import com.heyskylark.azukiloveisland.factory.SeasonFactory.Companion.DEFAULT_SEASON_NUMBER
import com.heyskylark.azukiloveisland.model.voting.GenderedInitialBracket
import com.heyskylark.azukiloveisland.util.TimeUtil.Companion.APRIL_16_2022_MILLI
import com.heyskylark.azukiloveisland.util.TimeUtil.Companion.APRIL_9_2022_MILLI
import com.heyskylark.azukiloveisland.util.TimeUtil.Companion.ONE_DAY_MILLI
import java.time.Instant

class InitialBracketFactory {
    companion object {
        const val DEFAULT_GENDERED_INITIAL_BRACKET_ID = "test-gendered-initial-bracket"
        val DEFAULT_VOTE_START_DATE = Instant.ofEpochMilli(APRIL_9_2022_MILLI)
        val DEFAULT_VOTE_END_DATE = Instant.ofEpochMilli(APRIL_16_2022_MILLI)

        fun createInitialBracket(): GenderedInitialBracket {
            return GenderedInitialBracket(
                id = DEFAULT_GENDERED_INITIAL_BRACKET_ID,
                seasonNumber = DEFAULT_SEASON_NUMBER,
                voteStartDate = DEFAULT_VOTE_START_DATE,
                voteDeadline = DEFAULT_VOTE_END_DATE,
                voteGapTimeMilliseconds = ONE_DAY_MILLI,
                maleBracketGroups = createBracketGroups(
                    startingId = 1,
                    numberOfGroups = 3
                ),
                femaleBracketGroups = createBracketGroups(
                    startingId = 5,
                    numberOfGroups = 3
                )
            )
        }
    }
}