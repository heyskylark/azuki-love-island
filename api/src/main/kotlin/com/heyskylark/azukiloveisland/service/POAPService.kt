package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.resource.dao.POAPDao
import com.heyskylark.azukiloveisland.resource.dao.VoteBracketDao
import com.heyskylark.azukiloveisland.dto.poap.POAPClaimRequestDto
import com.heyskylark.azukiloveisland.dto.poap.POAPClaimResponseDto
import com.heyskylark.azukiloveisland.dto.poap.POAPLoadRequestDto
import com.heyskylark.azukiloveisland.model.poap.POAPClaimUrl
import com.heyskylark.azukiloveisland.model.poap.SeasonPOAP
import com.heyskylark.azukiloveisland.model.voting.GenderedInitialBracket
import com.heyskylark.azukiloveisland.serialization.ErrorResponse
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.BracketErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.POAPErrorCodes
import com.heyskylark.azukiloveisland.util.HttpRequestUtil
import java.net.URL
import java.time.Instant
import java.time.ZoneOffset
import org.springframework.dao.DataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component("poapService")
class POAPService(
    private val bracketService: BracketService,
    private val poapDao: POAPDao,
    private val voteBracketDao: VoteBracketDao,
    private val httpRequestUtil: HttpRequestUtil,
): BaseService() {
    fun loadPOAPLinksToLatestSeason(
        poapLoadRequestDto: POAPLoadRequestDto
    ): ServiceResponse<Unit> {
        val initialBracketResponse = bracketService.getLatestSeasonBracket()
        val initialBracket = if (initialBracketResponse.isSuccess()) {
            (initialBracketResponse.getSuccessValue() as? GenderedInitialBracket)
                ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)
        } else {
            val errorResponse = initialBracketResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val latestSeasonNumber = initialBracket.seasonNumber

        return validatePOAPLoad(latestSeasonNumber, poapLoadRequestDto) ?: run {
            val parsedPOAPURLs = poapLoadRequestDto.poapLinks
                .split("\r?\n|\r".toRegex())
                .filter { it.isNotBlank() }
                .associate { poapMapKey(it) to POAPClaimUrl(url = URL(it)) }

            val seasonPoap = SeasonPOAP(
                seasonNumber = latestSeasonNumber,
                claimStarts = Instant.ofEpochMilli(poapLoadRequestDto.claimStartDateMilli),
                claimEnds = Instant.ofEpochMilli(poapLoadRequestDto.claimEndDateMilli),
                claimUrls = parsedPOAPURLs
            )

            try {
                poapDao.save(seasonPoap)

                ServiceResponse.successResponse()
            } catch (e: IllegalArgumentException) {
                ServiceResponse.errorResponse(POAPErrorCodes.PROBLEM_CREATING_POAP_CLAIM)
            }
        }
    }

    private fun validatePOAPLoad(
        seasonNumber: Int,
        poapLoadRequestDto: POAPLoadRequestDto
    ): ServiceResponse<Unit>? {
        poapDao.findByIdOrNull(seasonNumber)?.let {
            return ServiceResponse.errorResponse(POAPErrorCodes.POAP_EXIST_FOR_SEASON)
        }
        validatePOAPClaimWindow(poapLoadRequestDto)?.let { return it }

        return null
    }

    private fun validatePOAPClaimWindow(poapLoadRequestDto: POAPLoadRequestDto): ServiceResponse<Unit>? {
        val now = Instant.now()
        val startDate = Instant.ofEpochMilli(poapLoadRequestDto.claimStartDateMilli)
        val endDate = Instant.ofEpochMilli(poapLoadRequestDto.claimEndDateMilli)

        if (startDate >= endDate) {
            return ServiceResponse.errorResponse(POAPErrorCodes.INVALID_CLAIM_WINDOWS)
        }

        if (endDate <= now) {
            return ServiceResponse.errorResponse(POAPErrorCodes.INVALID_CLAIM_END_WINDOW)
        }

        return null
    }

    fun claimPOAP(
        seasonNumber: Int,
        poapClaimRequestDto: POAPClaimRequestDto
    ): ServiceResponse<POAPClaimResponseDto> {
        val ip = httpRequestUtil.getClientIpAddressIfServletRequestExist()

        return retryablePOAPClaim(
            ip = ip,
            seasonNumber = seasonNumber,
            poapClaimRequestDto = poapClaimRequestDto
        )
    }

    @Retryable(value = [DataAccessException::class], maxAttempts = 10)
    private fun retryablePOAPClaim(
        ip: String,
        seasonNumber: Int,
        poapClaimRequestDto: POAPClaimRequestDto
    ): ServiceResponse<POAPClaimResponseDto> {
        val seasonPOAPs = poapDao.findByIdOrNull(seasonNumber)
            ?: return ServiceResponse.errorResponse(POAPErrorCodes.SEASON_POAP_NOT_FOUND)

        return validatePOAPClaim(
            ip = ip,
            seasonNumber = seasonNumber,
            seasonPOAPs = seasonPOAPs,
            twitterHandle = poapClaimRequestDto.twitterHandle
        ) ?: run {
            val poapMap = seasonPOAPs.claimUrls.toMutableMap()
            val claimablePoap = poapMap.map { it.value }.find { it.claimedBy == null }
                ?: return ServiceResponse.errorResponse(POAPErrorCodes.NO_CLAIMABLE_POAPS_AVAILABLE)

            val claimedPOAP = claimablePoap.copy(
                claimedBy = poapClaimRequestDto.twitterHandle,
                claimedByIp = ip
            )

            poapMap[poapMapKey(claimedPOAP.url.toString())] = claimedPOAP

            val updatedSeasonPOAP = seasonPOAPs.copy(
                claimUrls = poapMap
            )

            try {
                poapDao.save(updatedSeasonPOAP)

                ServiceResponse.successResponse(POAPClaimResponseDto(claimablePoap.url))
            } catch (e: IllegalArgumentException) {
                ServiceResponse.errorResponse(POAPErrorCodes.PROBLEM_CLAIMING_POAP)
            }
        }
    }

    fun validatePOAPClaim(
        ip: String,
        seasonNumber: Int,
        seasonPOAPs: SeasonPOAP,
        twitterHandle: String
    ): ServiceResponse<POAPClaimResponseDto>? {
        validatePOAPClaimIsActive(seasonPOAPs)?.let { return it }
        validatePOAPClaimsAvailable(seasonPOAPs)?.let { return it }
        validateIfUserHasClaimedPoap(
            ip = ip,
            twitterHandle = twitterHandle,
            seasonPOAPs = seasonPOAPs
        )?.let { return it }
        validateUserVotedAndIsAbleToClaimPoap(
            seasonNumber = seasonNumber,
            twitterHandle = twitterHandle
        )?.let { return it }

        return null
    }

    private fun validatePOAPClaimIsActive(seasonPOAPs: SeasonPOAP): ServiceResponse<POAPClaimResponseDto>? {
        val now = Instant.now()

        if (now < seasonPOAPs.claimStarts) {
            return ServiceResponse.errorResponse(POAPErrorCodes.POAP_CLAIM_WINDOW_NOT_OPENED_YET)
        }

        if (now > seasonPOAPs.claimEnds) {
            return ServiceResponse.errorResponse(POAPErrorCodes.POAP_CLAIM_WINDOW_CLOSED)
        }

        return null
    }

    private fun validatePOAPClaimsAvailable(seasonPOAPs: SeasonPOAP): ServiceResponse<POAPClaimResponseDto>? {
        val claimablePOAPs= seasonPOAPs.claimUrls.filter { it.value.claimedBy == null }.toList()

        if (claimablePOAPs.isEmpty()) {
            return ServiceResponse.errorResponse(POAPErrorCodes.NO_CLAIMABLE_POAPS_AVAILABLE)
        }

        return null
    }

    fun canClaimPOAP(
        ip: String,
        twitterHandle: String,
        seasonNumber: Int
    ): Boolean {
        val seasonPOAPs = poapDao.findByIdOrNull(seasonNumber)
            ?: return false

        validatePOAPClaimIsActive(seasonPOAPs)?.let { return false }
        validatePOAPClaimsAvailable(seasonPOAPs)?.let { return false }
        validateIfUserHasClaimedPoap(
            ip = ip,
            twitterHandle = twitterHandle,
            seasonPOAPs = seasonPOAPs
        )?.let { return false }
        validateUserVotedAndIsAbleToClaimPoap(
            seasonNumber = seasonNumber,
            twitterHandle = twitterHandle
        )?.let { return false }

        return true
    }

    private fun validateIfUserHasClaimedPoap(
        ip: String,
        twitterHandle: String,
        seasonPOAPs: SeasonPOAP
    ): ServiceResponse<POAPClaimResponseDto>? {
        return seasonPOAPs.claimUrls
            .map { it.value}
            .find { it.claimedBy?.lowercase() == twitterHandle.lowercase() || it.claimedByIp == ip }?.let {
                if (it.claimedBy == twitterHandle) {
                    ServiceResponse.errorResponse(POAPErrorCodes.USER_ALREADY_CLAIMED_POAP)
                } else {
                    ServiceResponse.errorResponse(POAPErrorCodes.POAP_ALREADY_CLAIMED_BY_IP)
                }
            }
    }

    private fun validateUserVotedAndIsAbleToClaimPoap(
        seasonNumber: Int,
        twitterHandle: String
    ): ServiceResponse<POAPClaimResponseDto>? {
        val usersVotes = voteBracketDao.findBySeasonNumberAndTwitterHandleIgnoreCase(
            seasonNumber = seasonNumber,
            twitterHandle = twitterHandle
        )

        if (usersVotes.isEmpty()) {
            return ServiceResponse.errorResponse(POAPErrorCodes.USER_IS_NOT_VALID_TO_CLAIM)
        }

        return null
    }

    private fun poapMapKey(poapLink: String): String {
        return poapLink.split("/").last { it.isNotBlank() }
    }
}
