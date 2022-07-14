package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.InitialBracketDao
import com.heyskylark.azukiloveisland.dto.bracket.BracketCreationRequestDto
import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.azuki.Gender
import com.heyskylark.azukiloveisland.model.voting.BracketGroup
import com.heyskylark.azukiloveisland.model.voting.GenderedInitialBracket
import com.heyskylark.azukiloveisland.model.voting.BracketType
import com.heyskylark.azukiloveisland.model.voting.InitialBracket
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.BracketErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.SeasonErrorCodes
import java.time.Instant
import org.springframework.stereotype.Component

@Component("bracketService")
class BracketService(
    private val seasonService: SeasonService,
    private val participantService: ParticipantService,
    private val initialBracketDao: InitialBracketDao
) : BaseService() {
    fun getLatestSeasonBracket(): ServiceResponse<out InitialBracket> {
        val latestInitBracket = initialBracketDao.findFirstByOrderBySeasonNumberDesc()
            ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)

        return ServiceResponse.successResponse(latestInitBracket)
    }

    fun getLatestSeasonBracketWithVotingStarted(): ServiceResponse<out InitialBracket> {
        val latestInitBracket = initialBracketDao.findFirstByVoteStartDateGreaterThanEqualOrderBySeasonNumberDesc(
            voteStartDate = Instant.now()
        ) ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)

        return ServiceResponse.successResponse(latestInitBracket)
    }

    fun generateLatestSeasonGenderedBracket(
        bracketCreationRequestDto: BracketCreationRequestDto
    ): ServiceResponse<GenderedInitialBracket> {
        val latestSeason = seasonService.getRawLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)
        val contestants = participantService.getNoneDtoSeasonsContestants(latestSeason.seasonNumber)

        return validateBracketContestants(
            seasonNumber = latestSeason.seasonNumber,
            contestants = contestants,
            type = BracketType.GENDERED,
            bracketCreationRequestDto = bracketCreationRequestDto
        ) ?: run {
            val shuffledMaleContestants = contestants.filter { it.gender == Gender.MALE }.shuffled()
            val shuffledFemaleContestants = contestants.filter { it.gender == Gender.FEMALE }.shuffled()

            var sortOrder = 0
            val maleBracketGroups = shuffledMaleContestants.chunked(2).map { contestantPair ->
                val bracketGroup = BracketGroup(
                    submissionId1 = contestantPair[0].id,
                    submissionId2 = contestantPair[1].id,
                    sortOrder = sortOrder
                )

                sortOrder++

                bracketGroup
            }.toSet()

            sortOrder = 0
            val femaleBracketGroups = shuffledFemaleContestants.chunked(2).map { contestantPair ->
                val bracketGroup = BracketGroup(
                    submissionId1 = contestantPair[0].id,
                    submissionId2 = contestantPair[1].id,
                    sortOrder = sortOrder
                )

                sortOrder++

                bracketGroup
            }.toSet()

            val genderedInitialBracket = GenderedInitialBracket(
                seasonNumber = latestSeason.seasonNumber,
                voteStartDate = Instant.ofEpochMilli(bracketCreationRequestDto.voteStartDateMilli),
                voteDeadline = Instant.ofEpochMilli(bracketCreationRequestDto.voteDeadlineMilli),
                voteGapTimeMilliseconds = bracketCreationRequestDto.voteGapTimeMilliseconds,
                maleBracketGroups = maleBracketGroups,
                femaleBracketGroups = femaleBracketGroups
            )

            initialBracketDao.save(genderedInitialBracket)

            ServiceResponse.successResponse(genderedInitialBracket)
        }
    }

    private fun validateBracketContestants(
        seasonNumber: Int,
        contestants: Set<Participant>,
        type: BracketType,
        bracketCreationRequestDto: BracketCreationRequestDto
    ): ServiceResponse<GenderedInitialBracket>? {
        val voteStartDate = Instant.ofEpochMilli(bracketCreationRequestDto.voteStartDateMilli)
        val voteDeadline = Instant.ofEpochMilli(bracketCreationRequestDto.voteDeadlineMilli)

        if (voteStartDate >= voteDeadline) {
            return ServiceResponse.errorResponse(BracketErrorCodes.INVALID_BRACKET_VOTE_DATES)
        }

        if (voteDeadline <= Instant.now()) {
            return ServiceResponse.errorResponse(BracketErrorCodes.INVALID_BRACKET_VOTE_DEADLINE)
        }

        validateVoteGapTime(
            contestants = contestants,
            startDate = voteStartDate,
            endDate = voteDeadline,
            voteGapTimeMilli = bracketCreationRequestDto.voteGapTimeMilliseconds
        )?.let { return it }

        initialBracketDao.findBySeasonNumber(seasonNumber)
            ?.let { return ServiceResponse.errorResponse(BracketErrorCodes.BRACKET_ALREADY_EXISTS_FOR_SEASON) }

        if (contestants.isEmpty()) {
            return ServiceResponse.errorResponse(BracketErrorCodes.NO_CONTESTANTS_TO_BRACKET)
        }

        if (contestants.size < 4) {
            return ServiceResponse.errorResponse(BracketErrorCodes.NOT_ENOUGH_CONTESTANTS)
        }

        if (contestants.size % 2 != 0) {
            return ServiceResponse.errorResponse(BracketErrorCodes.UN_EVEN_NUM_CONTESTANTS)
        }

        if (type == BracketType.GENDERED) {
            if (contestants.any { it.gender == Gender.UNDETERMINED }) {
                return ServiceResponse.errorResponse(BracketErrorCodes.UNDETERMINED_CONTESTANT_GENDER)
            }

            val maleContestants = contestants.filter { it.gender == Gender.MALE }
            val femaleContestants = contestants.filter { it.gender == Gender.FEMALE }
            if (maleContestants.size != femaleContestants.size) {
                return ServiceResponse.errorResponse(BracketErrorCodes.UN_EVEN_GENDER_SPLIT)
            }
        }

        return null
    }

    private fun validateVoteGapTime(
        contestants: Set<Participant>,
        startDate: Instant,
        endDate: Instant,
        voteGapTimeMilli: Long?
    ): ServiceResponse<GenderedInitialBracket>? {
        voteGapTimeMilli?.let { voteGapMilli ->
            val numOfRounds = getNumberOfRounds(contestants.size / 2)
            val lastRoundStartDatePlusVoteGapBuffer = startDate.plusMillis(voteGapMilli * numOfRounds)

            if (lastRoundStartDatePlusVoteGapBuffer > endDate) {
                return ServiceResponse.errorResponse(BracketErrorCodes.INVALID_VOTE_GAP_WITH_END_TIME)
            }
        }

        return null
    }

    private fun getNumberOfRounds(contestants: Int): Int {
        var groupCount = contestants / 2 // Number of contestants grouped into 2
        var numOfBrackets = 1
        var loops = 0

        while (groupCount > 1) {
            groupCount /= 2

            if (loops > 10) {
                throw RuntimeException("Somehow got caught in a loop, brackets will never get this big...")
            }
            loops++
            numOfBrackets++
        }

        return numOfBrackets
    }

    data class BracketPrint(
        val data: String
    )

    fun printLatestSeasonBracket(): ServiceResponse<BracketPrint> {
        val contestants = (participantService.getLatestSeasonContestants().getSuccessValue()?.participants ?: emptySet())
            .associateBy { participant ->
                participant.id
            }
        val latestSeason = seasonService.getRawLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)
        val latestInitBracket = initialBracketDao.findBySeasonNumber(latestSeason.seasonNumber)

        val bracket = latestInitBracket as GenderedInitialBracket
        val sb = StringBuilder("FEMALE:")
        sb.appendLine()
        bracket.femaleBracketGroups.forEach {
            val first = contestants[it.submissionId1]
            val second = contestants[it.submissionId2]
            sb.append("${first!!.azukiId} | ${first.twitterHandle}")
            sb.append(" vs. ")
            sb.append("${second!!.azukiId} | ${second.twitterHandle}")
            sb.appendLine()
        }

        sb.appendLine()
        sb.append("MALE:")
        sb.appendLine()
        bracket.maleBracketGroups.forEach {
            val first = contestants[it.submissionId1]
            val second = contestants[it.submissionId2]
            sb.append("${first!!.azukiId} | ${first.twitterHandle}")
            sb.append(" vs. ")
            sb.append("${second!!.azukiId} | ${second.twitterHandle}")
            sb.appendLine()
        }

        LOG.info(sb.toString())

        return ServiceResponse.successResponse(BracketPrint(sb.toString()))
    }
}
