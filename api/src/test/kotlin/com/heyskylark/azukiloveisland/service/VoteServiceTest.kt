package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.VoteBracketDao
import com.heyskylark.azukiloveisland.factory.InitialBracketFactory.Companion.createInitialBracket
import com.heyskylark.azukiloveisland.factory.ParticipantFactory.Companion.createParticipant
import com.heyskylark.azukiloveisland.factory.VoteBracketFactory.Companion.createVotes
import com.heyskylark.azukiloveisland.factory.VoteBracketFactory.Companion.getListOfGenderedVoteBracketsForUser
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.util.HttpRequestUtil
import com.heyskylark.azukiloveisland.util.TimeUtil.Companion.APRIL_9_2022_MILLI
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
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

    private lateinit var voteService: VoteService

    @BeforeEach
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
    fun `successfully get latest season voteBracket for user that has NOT voted on the current open round`() {
        every { httpRequestUtil.getClientIpAddressIfServletRequestExist() } returns DEFAULT_IP

        val mockInitialBracket = createInitialBracket()
        every { bracketService.getLatestSeasonBracket() } returns ServiceResponse.successResponse(mockInitialBracket)

        every { timeService.getNow() } returns Instant.ofEpochMilli(APRIL_9_2022_MILLI)
            .plusMillis((mockInitialBracket.voteGapTimeMilliseconds ?: 0) + 1)

        every {
            voteBracketDao.findByIpAndSeasonNumber(DEFAULT_IP, mockInitialBracket.seasonNumber)
        } returns getListOfGenderedVoteBracketsForUser(
            ip = DEFAULT_IP,
            twitterHandle = DEFAULT_TWITTER_HANDLE,
            seasonNumber = mockInitialBracket.seasonNumber,
            maleBracketGroups = mockInitialBracket.maleBracketGroups,
            femaleBracketGroups = mockInitialBracket.femaleBracketGroups,
            numOfRounds = 1
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

        every {
            voteBracketDao.findBySeasonNumberAndBracketNumber(
                seasonNumber = mockInitialBracket.seasonNumber,
                bracketNumber = 1
            )
        } returns createVotes(
            seasonNumber = mockInitialBracket.seasonNumber,
            bracketNumber = 1,
            initialMaleBracketGroup = mockInitialBracket.maleBracketGroups,
            initialFemaleBracketGroup = mockInitialBracket.femaleBracketGroups,
            numOfVotes = 10
        )

        val response = voteService.getLatestVoteBracketForLatestSeason()

        assertThat(response.isSuccess()).isTrue

        val successValue = response.getSuccessValue()

        assertThat(successValue).isNotNull
        assertThat(successValue!!.seasonNumber).isEqualTo(1)
        assertThat(successValue.roundNumber).isEqualTo(1)
        assertThat(successValue.twitterHandle).isEqualTo(DEFAULT_TWITTER_HANDLE)
        assertThat(successValue.finishedVoting).isFalse
        assertThat(successValue.finalRound).isFalse
    }

    @Test
    fun `successfully get latest season voteBracket for user that HAS voted on the current open round`() {
        every { httpRequestUtil.getClientIpAddressIfServletRequestExist() } returns DEFAULT_IP

        val mockInitialBracket = createInitialBracket()
        every { bracketService.getLatestSeasonBracket() } returns ServiceResponse.successResponse(mockInitialBracket)

        every { timeService.getNow() } returns Instant.ofEpochMilli(APRIL_9_2022_MILLI)
            .plusMillis((mockInitialBracket.voteGapTimeMilliseconds ?: 0) + 1)

        every {
            voteBracketDao.findByIpAndSeasonNumber(DEFAULT_IP, mockInitialBracket.seasonNumber)
        } returns getListOfGenderedVoteBracketsForUser(
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

        every {
            voteBracketDao.findBySeasonNumberAndBracketNumber(
                seasonNumber = mockInitialBracket.seasonNumber,
                bracketNumber = 1
            )
        } returns createVotes(
            seasonNumber = mockInitialBracket.seasonNumber,
            bracketNumber = 1,
            initialMaleBracketGroup = mockInitialBracket.maleBracketGroups,
            initialFemaleBracketGroup = mockInitialBracket.femaleBracketGroups,
            numOfVotes = 10
        )

        val response = voteService.getLatestVoteBracketForLatestSeason()

        assertThat(response.isSuccess()).isTrue

        val successValue = response.getSuccessValue()

        assertThat(successValue).isNotNull
        assertThat(successValue!!.seasonNumber).isEqualTo(1)
        assertThat(successValue.roundNumber).isEqualTo(1)
        assertThat(successValue.twitterHandle).isEqualTo(DEFAULT_TWITTER_HANDLE)
        assertThat(successValue.finishedVoting).isTrue
        assertThat(successValue.finalRound).isFalse
    }

    @Test
    fun `successfully get latest season voteBracket for user that is on the final round`() {
        every { httpRequestUtil.getClientIpAddressIfServletRequestExist() } returns DEFAULT_IP

        val mockInitialBracket = createInitialBracket()
        every { bracketService.getLatestSeasonBracket() } returns ServiceResponse.successResponse(mockInitialBracket)

        every { timeService.getNow() } returns Instant.ofEpochMilli(APRIL_9_2022_MILLI)
            .plusMillis((mockInitialBracket.voteGapTimeMilliseconds ?: 0) * 2 + 1)

        every {
            voteBracketDao.findByIpAndSeasonNumber(DEFAULT_IP, mockInitialBracket.seasonNumber)
        } returns getListOfGenderedVoteBracketsForUser(
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

        every {
            voteBracketDao.findBySeasonNumberAndBracketNumber(
                seasonNumber = mockInitialBracket.seasonNumber,
                bracketNumber = 2
            )
        } returns createVotes(
            seasonNumber = mockInitialBracket.seasonNumber,
            bracketNumber = 2,
            initialMaleBracketGroup = mockInitialBracket.maleBracketGroups,
            initialFemaleBracketGroup = mockInitialBracket.femaleBracketGroups,
            numOfVotes = 10
        )

        val response = voteService.getLatestVoteBracketForLatestSeason()

        assertThat(response.isSuccess()).isTrue

        val successValue = response.getSuccessValue()

        assertThat(successValue).isNotNull
        assertThat(successValue!!.seasonNumber).isEqualTo(1)
        assertThat(successValue.roundNumber).isEqualTo(2)
        assertThat(successValue.twitterHandle).isEqualTo(DEFAULT_TWITTER_HANDLE)
        assertThat(successValue.finishedVoting).isFalse
        assertThat(successValue.finalRound).isTrue
    }
}