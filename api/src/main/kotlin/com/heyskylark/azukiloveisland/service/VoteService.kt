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
import org.springframework.stereotype.Component

@Component("voteService")
class VoteService(
    private val seasonService: SeasonService,
    private val bracketService: BracketService,
    private val voteBracketDao: VoteBracketDao
) : BaseService() {
    fun vote(voteRequestDto: VoteRequestDto): ServiceResponse<VoteBracket> {
        val ip = HttpRequestUtil.getClientIpAddressIfServletRequestExist()

        val latestSeason = seasonService.getLatestSeason()
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
                seasonNumber = latestSeason.seasonNumber,
                bracketNumber = (previousBracket?.bracketNumber ?: 0) + 1,
                maleBracketGroups = voteRequestDto.maleBracketGroups,
                femaleBracketGroups = voteRequestDto.femaleBracketGroups
            )

            voteBracketDao.save(voteBracket)

            if (voteBracket.bracketNumber == (initialBracket.numberOfBrackets() - 1)) {
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
        validateIfUserCanVoteInSeason(ip, latestSeasonNumber)?.let { return it }

        // Quick check is to check if num of groups are half of the previous bracket
        val previousCombinedBracketGroupSize = (previousBracket?.combinedGroup ?: initialBracket.combinedGroups).size
        val currentCombinedBracketGroupSize = (voteRequestDto.maleBracketGroups + voteRequestDto.femaleBracketGroups).size
        if (currentCombinedBracketGroupSize != previousCombinedBracketGroupSize / 2) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_NUM_OF_BRACKET_GROUPS)
        }

        // Reject votes that pass the final bracket (for this round final bracket might be second to last)
        val previousBracketNum = previousBracket?.bracketNumber ?: 0
        val lastVotableBracket = initialBracket.numberOfBrackets()
        // TODO: If we end up wanting the final vote on the website we should change this to ">"
        if ((previousBracketNum + 1) >= lastVotableBracket) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_VOTE_BRACKET)
        }

        // check if submissions are valid combinations from the previous bracket groups
        // TODO: Later on we need to support different voting types outside of gendered
        val maleBracketGroups = (previousBracket as? GenderedVoteBracket)?.maleBracketGroups
            ?: (initialBracket as GenderedInitialBracket).maleBracketGroups
        validateVotedGroups(
            previousVoteId = previousBracket?.id ?: initialBracket.id,
            currentGroup = voteRequestDto.maleBracketGroups,
            previousGroup = maleBracketGroups
        )?.let { return it }

        val femaleBracketGroups = (previousBracket as? GenderedVoteBracket)?.femaleBracketGroups
            ?: (initialBracket as GenderedInitialBracket).femaleBracketGroups
        validateVotedGroups(
            previousVoteId = previousBracket?.id ?: initialBracket.id,
            currentGroup = voteRequestDto.femaleBracketGroups,
            previousGroup = femaleBracketGroups
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
        previousGroup: Set<BracketGroup>
    ): ServiceResponse<VoteBracket>? {
        val previousGroupPairs: List<Set<String>> = previousGroup
            .toList()
            .sortedBy { it.sortOrder }
            .map {
                setOf(it.submissionId1, it.submissionId2)
            }

        var previousGroupIndex = 0
        currentGroup.toList().sortedBy { it.sortOrder }.forEach { bracketGroup ->
            val firstContestantId = bracketGroup.submissionId1
            val secondContestantId = bracketGroup.submissionId2

            if (
                !previousGroupPairs[previousGroupIndex].contains(firstContestantId) ||
                !previousGroupPairs[previousGroupIndex + 1].contains(firstContestantId)
            ) {
                LOG.error("FirstContestantId $secondContestantId is not valid. Previous vote: $previousVoteId")
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
            }

            if (
                !previousGroupPairs[previousGroupIndex].contains(secondContestantId) ||
                !previousGroupPairs[previousGroupIndex + 1].contains(secondContestantId)
            ) {
                LOG.error("SecondContestantId $secondContestantId is not valid. Previous vote: $previousVoteId")
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
            }

            previousGroupIndex += 2
        }

        return null
    }
}
