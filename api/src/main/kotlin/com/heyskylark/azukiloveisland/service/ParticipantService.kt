package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.InitialBracketDao
import com.heyskylark.azukiloveisland.dao.ParticipantDao
import com.heyskylark.azukiloveisland.dto.ParticipantSubmissionDto
import com.heyskylark.azukiloveisland.dto.participant.ParticipantCountDto
import com.heyskylark.azukiloveisland.dto.participant.ParticipantResponseDto
import com.heyskylark.azukiloveisland.dto.participant.SeasonParticipantsResponseDto
import com.heyskylark.azukiloveisland.model.azuki.AzukiInfo
import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.season.Season
import com.heyskylark.azukiloveisland.serialization.ErrorResponse
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.ParticipantErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.SeasonErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.Web3ErrorCodes
import com.heyskylark.azukiloveisland.util.isValidTwitterHandle
import org.springframework.stereotype.Component

@Component("participantService")
class ParticipantService(
    private val azukiWeb3Service: AzukiWeb3Service,
    private val seasonService: SeasonService,
    private val participantDao: ParticipantDao,
    private val initialBracketDao: InitialBracketDao
) : BaseService() {
    companion object {
        private const val MAX_AZUKI_ID = 9999
        private const val MAX_QUOTE_LENGTH = 100
        private const val MAX_BIO_LENGTH = 200
        private const val MAX_HOBBIES = 5
    }

    fun getLatestSeasonContestants(): ServiceResponse<SeasonParticipantsResponseDto> {
        val latestInitialBracket = initialBracketDao.findFirstByOrderBySeasonNumberDesc()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)

        val participantIds = latestInitialBracket.combinedGroups.map {
            it.submissionId2?.let { sub2 ->
                listOf(it.submissionId1, sub2)
            } ?: listOf(it.submissionId1)
        }.flatten().toSet()

        val participants = participantDao.findAllById(participantIds)
            .map { ParticipantResponseDto(it) }
            .toSet()

        return ServiceResponse.successResponse(
            SeasonParticipantsResponseDto(
                seasonNumber = latestInitialBracket.seasonNumber,
                participants = participants
            )
        )
    }

    fun getSeasonContestants(seasonNumber: Int): ServiceResponse<SeasonParticipantsResponseDto> {
        seasonService.getRawSeason(seasonNumber)
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.SEASON_DOES_NOT_EXIST)

        val participants = participantDao.findBySeasonNumberAndSubmitted(seasonNumber, submitted = true).map {
            ParticipantResponseDto(it)
        }.toSet()

        return ServiceResponse.successResponse(
            SeasonParticipantsResponseDto(
                seasonNumber = seasonNumber,
                participants = participants
            )
        )
    }

    fun getNoneDtoSeasonsContestants(seasonNumber: Int): Set<Participant> {
        return participantDao.findBySeasonNumberAndSubmitted(seasonNumber, true)
    }

    fun getLatestSeasonSubmissions(): ServiceResponse<Set<ParticipantResponseDto>> {
        val latestSeason = seasonService.getRawLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)
        return ServiceResponse.successResponse(
            participantDao.findBySeasonNumber(latestSeason.seasonNumber).map { ParticipantResponseDto(it) }.toSet()
        )
    }

    fun getSeasonSubmissions(
        seasonNumber: Int): ServiceResponse<SeasonParticipantsResponseDto> {
        seasonService.getRawSeason(seasonNumber)
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.SEASON_DOES_NOT_EXIST)

        val participants = participantDao.findBySeasonNumber(seasonNumber).map { ParticipantResponseDto(it) }.toSet()

        return ServiceResponse.successResponse(
            SeasonParticipantsResponseDto(
                seasonNumber = seasonNumber,
                participants = participants
            )
        )
    }

    fun getLatestSeasonSubmissionCount(): ServiceResponse<ParticipantCountDto> {
        val latestSeason = seasonService.getRawLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)
        val count = participantDao.findBySeasonNumber(latestSeason.seasonNumber).count()

        return ServiceResponse.successResponse(ParticipantCountDto(count))
    }

    fun getSubmissionCount(seasonNumber: Int): ServiceResponse<ParticipantCountDto> {
        seasonService.getRawSeason(seasonNumber)
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.SEASON_DOES_NOT_EXIST)
        val count = participantDao.findBySeasonNumber(seasonNumber).count()

        return ServiceResponse.successResponse(ParticipantCountDto(count))
    }

    fun getParticipant(participantId: String): ServiceResponse<Participant?> {
        return ServiceResponse.successResponse(participantDao.findById(participantId).orElse(null))
    }

    fun submitParticipant(participantSubmissionDto: ParticipantSubmissionDto): ServiceResponse<ParticipantResponseDto> {
        val latestSeason = seasonService.getRawLatestSeason()
        if (latestSeason == null || !latestSeason.submissionActive) {
            return ServiceResponse.errorResponse(ParticipantErrorCodes.SEASON_SUBMISSIONS_ARE_NOT_ACTIVE)
        }

        participantSubmissionDto.azukiId.takeIf { it < 0 || it > MAX_AZUKI_ID }
            ?.let { return ServiceResponse.errorResponse(Web3ErrorCodes.INVALID_NFT_ID) }

        val azukiInfoResponse = azukiWeb3Service.fetchAzukiInfo(participantSubmissionDto.azukiId)
        val azukiInfo = if (azukiInfoResponse.isSuccess()) {
            azukiInfoResponse.getSuccessValue() ?: return ServiceResponse.errorResponse(ParticipantErrorCodes.AZUKI_INFO_MISSING)
        } else {
            val errorResponse = azukiInfoResponse as ErrorResponse

            LOG.info("There was an error when trying to fetch Azuki info form the azukiWeb3Service: ${errorResponse.errorCode.message}")
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val parsedHobbies = participantSubmissionDto.hobbies
            ?.split(",")
            ?.map { it.trim() }
            ?.toSet()

        return validateParticipantSubmission(latestSeason, participantSubmissionDto, azukiInfo, parsedHobbies) ?: run {
            val participant = Participant(
                azukiId = participantSubmissionDto.azukiId,
                ownerAddress = azukiInfo.ownerAddress,
                imageUrl = azukiInfo.azukiImageUrl,
                quote = participantSubmissionDto.quote,
                bio = participantSubmissionDto.bio,
                hobbies = parsedHobbies,
                backgroundTrait = azukiInfo.backgroundTrait,
                twitterHandle = participantSubmissionDto.twitterHandle,
                seasonNumber = latestSeason.seasonNumber
            )

            participantDao.save(participant)

            ServiceResponse.successResponse(ParticipantResponseDto(participant))
        }
    }

    private fun validateParticipantSubmission(
        latestSeason: Season,
        participantSubmissionDto: ParticipantSubmissionDto,
        azukiInfo: AzukiInfo,
        parsedHobbies: Set<String>?
    ): ServiceResponse<ParticipantResponseDto>? {
        // TODO: Add validation to reject submissions after certain date
        //  ...although manually closing submissions is more flexible

        if (!participantSubmissionDto.twitterHandle.isValidTwitterHandle()) {
            return ServiceResponse.errorResponse(ParticipantErrorCodes.TWITTER_HANDLE_MISSING)
        }

        participantSubmissionDto.quote.takeIf { it.count() > MAX_QUOTE_LENGTH }
            ?.let { return ServiceResponse.errorResponse(ParticipantErrorCodes.BIO_TOO_LONG_ERROR) }

        participantSubmissionDto.bio?.takeIf { it.count() > MAX_BIO_LENGTH }
            ?.let { return ServiceResponse.errorResponse(ParticipantErrorCodes.BIO_TOO_LONG_ERROR) }

        parsedHobbies?.takeIf { it.size > MAX_HOBBIES }
            ?.let { return ServiceResponse.errorResponse(ParticipantErrorCodes.HOBBIES_TOO_LONG_ERROR) }

        validateAzukiId(participantSubmissionDto.azukiId, latestSeason)?.let { return it }

        validateOwnerAddress(azukiInfo.ownerAddress, latestSeason)?.let { return it }

        validateTwitterHandle(participantSubmissionDto.twitterHandle, latestSeason)?.let { return it }

        return null
    }

    private fun validateAzukiId(azukiId: Long, season: Season): ServiceResponse<ParticipantResponseDto>? {
        participantDao.findByAzukiIdAndSeasonNumber(azukiId, season.seasonNumber)?.let {
            return ServiceResponse.errorResponse(ParticipantErrorCodes.AZUKI_ID_ALREADY_EXISTS)
        }

        return null
    }

    private fun validateOwnerAddress(ownerAddress: String, season: Season): ServiceResponse<ParticipantResponseDto>? {
        participantDao.findByOwnerAddressAndSeasonNumber(ownerAddress, season.seasonNumber)?.let {
            return ServiceResponse.errorResponse(ParticipantErrorCodes.OWNER_ADDRESS_EXISTS)
        }

        return null
    }

    private fun validateTwitterHandle(twitterHandle: String, season: Season): ServiceResponse<ParticipantResponseDto>? {
        participantDao.findByTwitterHandleAndSeasonNumber(twitterHandle, season.seasonNumber)?.let {
            return ServiceResponse.errorResponse(ParticipantErrorCodes.TWITTER_HANDLE_EXISTS)
        }

        return null
    }
}
