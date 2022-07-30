package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.POAPDao
import com.heyskylark.azukiloveisland.dao.VoteBracketDao
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
        poapDao.findById(seasonNumber).orElse(null)?.let {
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

        val seasonPOAPs = poapDao.findById(seasonNumber).orElse(null)
            ?: return ServiceResponse.errorResponse(POAPErrorCodes.SEASON_POAP_NOT_FOUND)

        return validatePOAPClaim(
            ip = ip,
            seasonNumber = seasonNumber,
            seasonPOAPs = seasonPOAPs,
            poapClaimRequestDto = poapClaimRequestDto
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

    private fun validatePOAPClaim(
        ip: String,
        seasonNumber: Int,
        seasonPOAPs: SeasonPOAP,
        poapClaimRequestDto: POAPClaimRequestDto
    ): ServiceResponse<POAPClaimResponseDto>? {
        validatePOAPClaimIsActive(seasonPOAPs)?.let { return it }
        validatePOAPClaimsAvailable(seasonPOAPs)?.let { return it }
        validateIfUserHasClaimedPoap(
            ip = ip,
            twitterHandle = poapClaimRequestDto.twitterHandle,
            seasonPOAPs = seasonPOAPs
        )?.let { return it }
        validateUserVotedAndIsAbleToClaimPoap(
            seasonNumber = seasonNumber,
            twitterHandle = poapClaimRequestDto.twitterHandle
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