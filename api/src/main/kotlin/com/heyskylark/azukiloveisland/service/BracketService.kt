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
        val latestSeason = seasonService.getLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)
        val latestInitBracket = initialBracketDao.findBySeasonNumber(latestSeason.seasonNumber)

        return ServiceResponse.successResponse(latestInitBracket)
    }

    fun generateLatestSeasonGenderedBracket(
        bracketCreationRequestDto: BracketCreationRequestDto
    ): ServiceResponse<GenderedInitialBracket> {
        val latestSeason = seasonService.getLatestSeason()
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

            ServiceResponse.successResponse(
                GenderedInitialBracket(
                    seasonNumber = latestSeason.seasonNumber,
                    voteDeadline = bracketCreationRequestDto.voteDeadline,
                    maleBracketGroups = maleBracketGroups,
                    femaleBracketGroups = femaleBracketGroups
                )
            )
        }
    }

    private fun validateBracketContestants(
        seasonNumber: Int,
        contestants: Set<Participant>,
        type: BracketType,
        bracketCreationRequestDto: BracketCreationRequestDto
    ): ServiceResponse<GenderedInitialBracket>? {
        if (bracketCreationRequestDto.voteDeadline <= Instant.now()) {
            return ServiceResponse.errorResponse(BracketErrorCodes.INVALID_BRACKET_VOTE_DEADLINE)
        }

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
}
