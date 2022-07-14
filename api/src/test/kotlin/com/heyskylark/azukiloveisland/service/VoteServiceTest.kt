package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.VoteBracketDao
import com.heyskylark.azukiloveisland.factory.InitialBracketFactory.Companion.createInitialBracket
import com.heyskylark.azukiloveisland.factory.ParticipantFactory.Companion.createParticipant
import com.heyskylark.azukiloveisland.factory.VoteBracketFactory.Companion.getListOfGenderedVoteBrackets
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.util.HttpRequestUtil
import com.heyskylark.azukiloveisland.util.TimeUtil.Companion.APRIL_9_2022_MILLI
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class VoteServiceTest {
    companion object {
        private const val DEFAULT_IP = "69.420.69.7"
        private const val DEFAULT_TWITTER_HANDLE = "degenman"
    }

    @MockK lateinit var bracketService: BracketService
    @MockK lateinit var participantService: ParticipantService
    @MockK lateinit var voteBracketDao: VoteBracketDao
    @MockK lateinit var httpRequestUtil: HttpRequestUtil
    @MockK lateinit var timeService: TimeService

    lateinit var voteService: VoteService

    @BeforeAll
    fun setUp() {
        voteService = VoteService(
            bracketService = bracketService,
            participantService = participantService,
            voteBracketDao = voteBracketDao,
            httpRequestUtil = httpRequestUtil,
            timeService = timeService
        )
    }

    @Test
    fun `successfully get latest season voteBracket for user that voted previous`() {
        every { httpRequestUtil.getClientIpAddressIfServletRequestExist() } returns DEFAULT_IP

        val mockInitialBracket = createInitialBracket()
        every {
            bracketService.getLatestSeasonBracket()
        } returns ServiceResponse.successResponse(mockInitialBracket)

        every { timeService.getNow() } returns Instant.ofEpochMilli(APRIL_9_2022_MILLI)
            .plusMillis(mockInitialBracket.voteGapTimeMilliseconds ?: 0)

        every {
            voteBracketDao.findByIpAndSeasonNumber(DEFAULT_IP, mockInitialBracket.seasonNumber)
        } returns getListOfGenderedVoteBrackets(
            ip = DEFAULT_IP,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            seasonNumber = mockInitialBracket.seasonNumber,
            maleBracketGroups = mockInitialBracket.maleBracketGroups,
            femaleBracketGroups = mockInitialBracket.femaleBracketGroups,
            numOfRounds = 2
        )

        every {
            participantService.getNoneDtoSeasonsContestants(mockInitialBracket.seasonNumber)
        } returns mockInitialBracket.combinedGroups.map {
            val subId2 = it.submissionId2
            if (subId2 != null) {
                listOf(
                    createParticipant(it.submissionId1, mockInitialBracket.seasonNumber),
                    createParticipant(subId2, mockInitialBracket.seasonNumber)
                )
            } else {
                listOf(createParticipant(it.submissionId1, mockInitialBracket.seasonNumber))
            }
        }.flatten().toSet()

        // TODO: Mock votes in the system
//        every {
//            voteBracketDao.findBySeasonNumberAndBracketNumber(
//                seasonNumber = mockInitialBracket.seasonNumber,
//                bracketNumber = 1
//            )
//        } {
//
//        }

        val response = voteService.getLatestVoteBracketForLatestSeason()
    }
}