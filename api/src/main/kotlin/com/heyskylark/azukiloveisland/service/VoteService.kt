package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.VoteBracketDao
import com.heyskylark.azukiloveisland.dto.vote.VoteRequestDto
import com.heyskylark.azukiloveisland.model.voting.BracketGroup
import com.heyskylark.azukiloveisland.model.voting.GenderedInitialBracket
import com.heyskylark.azukiloveisland.model.voting.GenderedVoteBracket
import com.heyskylark.azukiloveisland.model.voting.InitialBracket
import com.heyskylark.azukiloveisland.model.voting.VoteBracket
import com.heyskylark.azukiloveisland.serialization.ErrorResponse
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.BracketErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.SeasonErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.VoteBracketErrorCodes
import com.heyskylark.azukiloveisland.util.HttpRequestUtil
import java.time.Instant
import org.springframework.stereotype.Component

@Component("voteService")
class VoteService(
    private val seasonService: SeasonService,
    private val bracketService: BracketService,
    private val voteBracketDao: VoteBracketDao
) : BaseService() {
    fun getLatestVoteBracketForLatestSeason(): ServiceResponse<VoteBracket> {
        val ip = HttpRequestUtil.getClientIpAddressIfServletRequestExist()

        val latestSeason = seasonService.getRawLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)

        val latestVoteBracket = voteBracketDao.findByIpAndSeasonNumber(ip, latestSeason.seasonNumber)
            .maxByOrNull {
                it.bracketNumber
            } ?: return ServiceResponse.errorResponse(VoteBracketErrorCodes.NO_VOTE_BRACKET_FOUND)

        return ServiceResponse.successResponse(latestVoteBracket)
    }

    fun vote(voteRequestDto: VoteRequestDto): ServiceResponse<VoteBracket> {
        val ip = HttpRequestUtil.getClientIpAddressIfServletRequestExist()

        val latestSeason = seasonService.getRawLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)

        val previousBracket = voteBracketDao.findByIpAndSeasonNumber(ip, latestSeason.seasonNumber)
            .maxByOrNull { it.bracketNumber }

        val initialBracketResponse = bracketService.getLatestSeasonBracket()
        val initialBracket = if (initialBracketResponse.isSuccess()) {
            initialBracketResponse.getSuccessValue()
                ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)
        } else {
            val errorResponse = initialBracketResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        return validateVote(
            ip = ip,
            latestSeasonNumber = latestSeason.seasonNumber,
            voteRequestDto = voteRequestDto,
            initialBracket = initialBracket,
            previousBracket = previousBracket
        ) ?: run {
            val voteBracket = GenderedVoteBracket(
                ip = ip,
                twitterHandle = voteRequestDto.twitterHandle,
                seasonNumber = latestSeason.seasonNumber,
                bracketNumber = (previousBracket?.bracketNumber ?: 0) + 1,
                maleBracketGroups = voteRequestDto.maleBracketGroups,
                femaleBracketGroups = voteRequestDto.femaleBracketGroups
            )

            voteBracketDao.save(voteBracket)

            // TODO: (initialBracket.numberOfBrackets() - 1) if want to end right before last bracket for Bobu vote
            if (voteBracket.bracketNumber == initialBracket.numberOfBrackets()) {
                updateVotesToFinishedVoting(ip, latestSeason.seasonNumber)
            }

            // Update all votes finishedVoting flag after final bracket
            ServiceResponse.successResponse(voteBracket)
        }
    }

    private fun updateVotesToFinishedVoting(ip: String, seasonNumber: Int) {
        val updatedVotes = voteBracketDao.findByIpAndSeasonNumber(ip, seasonNumber).map { previousVote ->
            // TODO: Need to make more generalized for when we have non-gendered brackets,
            //  should just change to left and right bracket?
            (previousVote as GenderedVoteBracket).copy(
                finishedVoting = true
            )
        }.toList()

        voteBracketDao.saveAll(updatedVotes)
    }

    private fun validateVote(
        ip: String,
        latestSeasonNumber: Int,
        voteRequestDto: VoteRequestDto,
        initialBracket: InitialBracket,
        previousBracket: VoteBracket?
    ): ServiceResponse<VoteBracket>? {
        // Check if voting has not started for the season
        if (initialBracket.voteStartDate > Instant.now()) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.VOTING_HAS_NOT_STARTED)
        }

        // Check if voting has ended for the season
        if (Instant.now() > initialBracket.voteDeadline) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.VOTING_HAS_ENDED)
        }

        validateIfUserCanVoteInSeason(ip, latestSeasonNumber)?.let { return it }

        // Twitter Handle Check
        if (previousBracket == null) {
            // If first vote validate twitter handle is not blank and is a valid username
            // Check if twitterHandle already exists in the db
            // TODO regex for twitter handle validation (maybe make utility func to check blank and regex)
            if (voteRequestDto.twitterHandle.isBlank()) {
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_TWITTER_HANDLE)
            }

            if (voteBracketDao.findByTwitterHandle(voteRequestDto.twitterHandle).isNotEmpty()) {
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.TWITTER_HANDLE_USED)
            }
        } else {
            val previousTwitterHandle = previousBracket.twitterHandle

            if (previousTwitterHandle != voteRequestDto.twitterHandle) {
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_TWITTER_HANDLE)
            }
        }

        // Quick check is to check if num of groups are half of the previous bracket
        val previousCombinedBracketGroupSize = (previousBracket?.combinedGroup ?: initialBracket.combinedGroups).size
        val currentCombinedBracketGroupSize = (voteRequestDto.maleBracketGroups + voteRequestDto.femaleBracketGroups).size
        val isFinalBracket = ((previousBracket?.bracketNumber ?: 0) + 1) == initialBracket.numberOfBrackets()
        val invalidFinalBracket = ((previousBracket?.bracketNumber ?: 0) + 1) == initialBracket.numberOfBrackets() &&
                previousCombinedBracketGroupSize == 2 &&
                currentCombinedBracketGroupSize != 2

        if (
            (isFinalBracket && invalidFinalBracket) ||
            (!isFinalBracket && currentCombinedBracketGroupSize != previousCombinedBracketGroupSize / 2)
        ) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_NUM_OF_BRACKET_GROUPS)
        }

        // Reject votes that pass the final bracket (for this round final bracket might be second to last)
        val previousBracketNum = previousBracket?.bracketNumber ?: 0
        val lastVotableBracket = initialBracket.numberOfBrackets()
        // TODO: If we don't want the final vote (for Bobu) then change to ">="
        if ((previousBracketNum + 1) > lastVotableBracket) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_VOTE_BRACKET)
        }

        // check if submissions are valid combinations from the previous bracket groups
        // TODO: Later on we need to support different voting types outside of gendered
        val maleBracketGroups = (previousBracket as? GenderedVoteBracket)?.maleBracketGroups
            ?: run {
                (initialBracket as GenderedInitialBracket).maleBracketGroups
            }
        validateVotedGroups(
            previousVoteId = previousBracket?.id ?: initialBracket.id,
            currentGroup = voteRequestDto.maleBracketGroups,
            previousGroup = maleBracketGroups,
            currentVoteBracketNum = previousBracketNum + 1,
            lastVotableBracketNum = lastVotableBracket
        )?.let { return it }

        val femaleBracketGroups = (previousBracket as? GenderedVoteBracket)?.femaleBracketGroups
            ?: run {
                (initialBracket as GenderedInitialBracket).femaleBracketGroups
            }
        validateVotedGroups(
            previousVoteId = previousBracket?.id ?: initialBracket.id,
            currentGroup = voteRequestDto.femaleBracketGroups,
            previousGroup = femaleBracketGroups,
            currentVoteBracketNum = previousBracketNum + 1,
            lastVotableBracketNum = lastVotableBracket
        )?.let { return it }

        return null
    }

    private fun validateIfUserCanVoteInSeason(ip: String, seasonNumber: Int): ServiceResponse<VoteBracket>? {
        val previousVotes = voteBracketDao.findByIpAndSeasonNumber(ip, seasonNumber)

        if (previousVotes.firstOrNull()?.finishedVoting == true) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.CANNOT_VOTE_AGAIN)
        }

        return null
    }

    private fun validateVotedGroups(
        previousVoteId: String,
        currentGroup: Set<BracketGroup>,
        previousGroup: Set<BracketGroup>,
        currentVoteBracketNum: Int,
        lastVotableBracketNum: Int
    ): ServiceResponse<VoteBracket>? {
        val previousGroupPairs: List<Set<String>> = previousGroup
            .toList()
            .sortedBy { it.sortOrder }
            .map {
                setOf(it.submissionId1, it.submissionId2!!)
            }

        var previousGroupIndex = 0
        if (previousGroupPairs.size == 1 && currentVoteBracketNum == lastVotableBracketNum) {
            val firstContestantId = currentGroup.first().submissionId1
            if (!previousGroupPairs[previousGroupIndex].contains(firstContestantId)) {
                LOG.error("FirstContestantId $firstContestantId is not valid. Previous vote: $previousVoteId")
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
            }
        } else {
            currentGroup.toList().sortedBy { it.sortOrder }.forEach { bracketGroup ->
                val firstContestantId = bracketGroup.submissionId1
                val secondContestantId = bracketGroup.submissionId2

                if (!previousGroupPairs[previousGroupIndex].contains(firstContestantId)) {
                    LOG.error("FirstContestantId $firstContestantId is not valid. Previous vote: $previousVoteId")
                    return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
                }

                if (!previousGroupPairs[previousGroupIndex + 1].contains(secondContestantId)) {
                    LOG.error("SecondContestantId $secondContestantId is not valid. Previous vote: $previousVoteId")
                    return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
                }

                previousGroupIndex += 2
            }
        }

        return null
    }
}
