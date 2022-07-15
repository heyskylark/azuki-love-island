package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.VoteBracketDao
import com.heyskylark.azukiloveisland.dto.vote.GenderedRoundWinners
import com.heyskylark.azukiloveisland.dto.vote.GenderedVoteBracketResponseDto
import com.heyskylark.azukiloveisland.dto.vote.GenderedVoteCountResults
import com.heyskylark.azukiloveisland.dto.vote.GenderedVoteRoundsResponse
import com.heyskylark.azukiloveisland.dto.vote.ParsedGenderedRoundWinners
import com.heyskylark.azukiloveisland.dto.vote.VoteCountResults
import com.heyskylark.azukiloveisland.dto.vote.VoteRequestDto
import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.voting.BracketGroup
import com.heyskylark.azukiloveisland.model.voting.GenderedInitialBracket
import com.heyskylark.azukiloveisland.model.voting.GenderedVoteBracket
import com.heyskylark.azukiloveisland.model.voting.InitialBracket
import com.heyskylark.azukiloveisland.model.voting.ParsedWinningResultsBracketGroup
import com.heyskylark.azukiloveisland.model.voting.VoteBracket
import com.heyskylark.azukiloveisland.model.voting.WinningResultsBracketGroup
import com.heyskylark.azukiloveisland.serialization.ErrorResponse
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.BracketErrorCodes
import com.heyskylark.azukiloveisland.service.errorcode.VoteBracketErrorCodes
import com.heyskylark.azukiloveisland.util.HttpRequestUtil
import com.heyskylark.azukiloveisland.util.isValidTwitterHandle
import kotlin.math.min
import org.springframework.stereotype.Component

@Component("voteService")
class VoteService(
    private val bracketService: BracketService,
    private val participantService: ParticipantService,
    private val voteBracketDao: VoteBracketDao,
    private val httpRequestUtil: HttpRequestUtil,
    private val timeService: TimeService
) : BaseService() {
    fun getLatestVoteBracketForLatestSeason(): ServiceResponse<GenderedVoteBracketResponseDto> {
        val ip = httpRequestUtil.getClientIpAddressIfServletRequestExist()

        val initialBracketResponse = bracketService.getLatestSeasonBracket()
        val initialBracket = if (initialBracketResponse.isSuccess()) {
            initialBracketResponse.getSuccessValue()
                ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)
        } else {
            val errorResponse = initialBracketResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val latestSeasonNumber = initialBracket.seasonNumber

        val lastClosedRound = fetchNumberOfRounds(initialBracket)
        if (lastClosedRound == 0) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.NO_VOTE_BRACKET_FOUND)
        }

        // Get users last vote
        val usersLatestVoteBracket = voteBracketDao.findByIpAndSeasonNumber(ip, latestSeasonNumber)
            .maxByOrNull {
                it.bracketNumber
            } ?: return ServiceResponse.errorResponse(VoteBracketErrorCodes.NO_VOTE_BRACKET_FOUND)

        // Get last rounds winners
        val resultsResponse = calculateRoundVotesForLatestSeason(
            initialBracket = initialBracket,
            roundNumber = lastClosedRound,
            isRoundByRoundVoting = initialBracket.voteGapTimeMilliseconds != null
        )

        val lastRoundsWinners = if (resultsResponse.isSuccess()) {
            resultsResponse.getSuccessValue()
                ?: return ServiceResponse.errorResponse(VoteBracketErrorCodes.WINNER_CALC_ISSUE)
        } else {
            val errorResponse = resultsResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val responseVoteBracket = GenderedVoteBracketResponseDto(
            twitterHandle = usersLatestVoteBracket.twitterHandle,
            seasonNumber = usersLatestVoteBracket.seasonNumber,
            bracketNumber = lastClosedRound,
            maleBracketGroups = lastRoundsWinners.maleWinners.map { BracketGroup(it) }.toSet(),
            femaleBracketGroups = lastRoundsWinners.femaleWinners.map { BracketGroup(it) }.toSet(),
            finishedVoting = usersLatestVoteBracket.bracketNumber == initialBracket.numOfBrackets ||
                    usersLatestVoteBracket.bracketNumber == (lastClosedRound + 1), // User voted on final round or on current open round
            finalRound = lastClosedRound >= (initialBracket.numOfBrackets - 1) // Last closed round is round before the final round or is completely finished
        )

        return ServiceResponse.successResponse(responseVoteBracket)
    }

    fun calculateParsedRoundVotesForLastSeason(roundNumber: Int): ServiceResponse<ParsedGenderedRoundWinners> {
        val initialBracketResponse = bracketService.getLatestSeasonBracket()
        val initialBracket = if (initialBracketResponse.isSuccess()) {
            initialBracketResponse.getSuccessValue()
                ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)
        } else {
            val errorResponse = initialBracketResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val latestSeasonNumber = initialBracket.seasonNumber

        val seasonParticipants = participantService.getNoneDtoSeasonsContestants(latestSeasonNumber)
        val participantMap = seasonParticipants.associateBy { it.id }

        // TODO: Later validate if season isn't over yet and block request
        val votes = if (initialBracket.voteGapTimeMilliseconds != null) {
            voteBracketDao.findBySeasonNumberAndBracketNumber(
                seasonNumber = latestSeasonNumber,
                bracketNumber = roundNumber
            )
        } else {
            voteBracketDao.findBySeasonNumberAndBracketNumberAndFinishedVoting(
                seasonNumber = latestSeasonNumber,
                bracketNumber = roundNumber,
                finishedVoting = true
            )
        }

        return try {
            val unparsed = calculateWinners(
                votes = votes,
                participants = seasonParticipants,
                roundNumber = roundNumber
            )

            val parsed = ParsedGenderedRoundWinners(
                maleWinners = unparsed.maleWinners.mapNotNull {
                    val sub1 = participantMap[it.submissionId1]
                    val sub2 = participantMap[it.submissionId2]

                    if (sub1 != null && sub2 != null) {
                        ParsedWinningResultsBracketGroup(
                            submission1 = sub1,
                            submission2 = sub2,
                            sub1VoteCount = it.sub1VoteCount,
                            sub2VoteCount = it.sub2VoteCount,
                            sortOrder = it.sortOrder
                        )
                    } else null
                }.toList().sortedBy { it.sortOrder },
                femaleWinners = unparsed.femaleWinners.mapNotNull {
                    val sub1 = participantMap[it.submissionId1]
                    val sub2 = participantMap[it.submissionId2]

                    if (sub1 != null && sub2 != null) {
                        ParsedWinningResultsBracketGroup(
                            submission1 = sub1,
                            submission2 = sub2,
                            sub1VoteCount = it.sub1VoteCount,
                            sub2VoteCount = it.sub2VoteCount,
                            sortOrder = it.sortOrder
                        )
                    } else null
                }.toList().sortedBy { it.sortOrder },
                roundNumber = roundNumber
            )

            ServiceResponse.successResponse(parsed)
        } catch (e: Exception) {
            LOG.error("There was a problem calculating the round members", e)
            ServiceResponse.errorResponse(VoteBracketErrorCodes.WINNER_CALC_ISSUE)
        }
    }

    fun calculateTotalVotesForLatestSeason(): ServiceResponse<GenderedVoteRoundsResponse> {
        val initialBracketResponse = bracketService.getLatestSeasonBracket()
        val initialBracket = if (initialBracketResponse.isSuccess()) {
            initialBracketResponse.getSuccessValue()
                ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)
        } else {
            val errorResponse = initialBracketResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val numOfRounds = fetchNumberOfRounds(initialBracket)
        if (numOfRounds == 0) {
            return ServiceResponse.successResponse(
                GenderedVoteRoundsResponse(seasonNumber = initialBracket.seasonNumber)
            )
        }

        val rounds = mutableListOf<GenderedRoundWinners>()
        for(roundNumber in 1..numOfRounds) {
            val resultsResponse = calculateRoundVotesForLatestSeason(
                initialBracket = initialBracket,
                roundNumber = roundNumber,
                isRoundByRoundVoting = initialBracket.voteGapTimeMilliseconds != null
            )
            if (resultsResponse.isSuccess()) {
                val results = resultsResponse.getSuccessValue()
                    ?: return ServiceResponse.errorResponse(VoteBracketErrorCodes.WINNER_CALC_ISSUE)

                rounds.add(results)
            } else {
                val errorResponse = resultsResponse as ErrorResponse
                return ServiceResponse.errorResponse(errorResponse.errorCode)
            }
        }

        return ServiceResponse.successResponse(
            GenderedVoteRoundsResponse(
                seasonNumber = initialBracket.seasonNumber,
                rounds = rounds.sortedBy { it.roundNumber }
            )
        )
    }

    private fun fetchNumberOfRounds(initialBracket: InitialBracket): Int {
        val now = timeService.getNow()
        val voteGapTime = initialBracket.voteGapTimeMilliseconds

        // If voting has ended or old school (full bracket fill where voteGapTime doesn't exist), return all rounds
        if (voteGapTime == null || now >= initialBracket.voteDeadline) {
            return initialBracket.numOfBrackets
        }

        val firstRoundLock = initialBracket.voteStartDate.plusMillis(voteGapTime)
        return if (now > firstRoundLock) {
            val startNowDifference = now.toEpochMilli() - initialBracket.voteStartDate.toEpochMilli()
            val maxRounds = initialBracket.numOfBrackets

            min((startNowDifference / voteGapTime).toInt(), maxRounds)
        } else 0
    }

    private fun calculateRoundVotesForLatestSeason(
        initialBracket: InitialBracket,
        roundNumber: Int,
        isRoundByRoundVoting: Boolean
    ): ServiceResponse<GenderedRoundWinners> {
        val bracketSeasonNumber = initialBracket.seasonNumber

        val seasonParticipants = participantService.getNoneDtoSeasonsContestants(bracketSeasonNumber)

        // TODO: Later validate if season isn't over yet and block request

        val votes = if (isRoundByRoundVoting) {
            voteBracketDao.findBySeasonNumberAndBracketNumber(
                seasonNumber = bracketSeasonNumber,
                bracketNumber = roundNumber
            )
        } else {
            voteBracketDao.findBySeasonNumberAndBracketNumberAndFinishedVoting(
                seasonNumber = bracketSeasonNumber,
                bracketNumber = roundNumber,
                finishedVoting = true
            )
        }

        return try {
            ServiceResponse.successResponse(
                calculateWinners(
                    votes = votes,
                    participants = seasonParticipants,
                    roundNumber = roundNumber
                )
            )
        } catch (e: Exception) {
            LOG.error("There was a problem calculating the round members", e)
            ServiceResponse.errorResponse(VoteBracketErrorCodes.WINNER_CALC_ISSUE)
        }
    }

    private fun calculateWinners(
        votes: List<VoteBracket>,
        participants: Set<Participant>,
        roundNumber: Int
    ): GenderedRoundWinners {
        val participantMap = participants.associateBy { it.id }

        // Map<SortOrder, Map<ParticipantId, VoteCount>>
        val sub1MaleVoteMap = mutableMapOf<Int, MutableMap<String, Int>>()
        val sub2MaleVoteMap = mutableMapOf<Int, MutableMap<String, Int>>()
        val sub1FemaleVoteMap = mutableMapOf<Int, MutableMap<String, Int>>()
        val sub2FemaleVoteMap = mutableMapOf<Int, MutableMap<String, Int>>()

        votes.forEach { voteBracket ->
            when (voteBracket) {
                is GenderedVoteBracket -> {
                    voteBracket.maleBracketGroups.forEach {  group ->
                        parseBracketGroup(
                            group = group,
                            sub1VoteMap = sub1MaleVoteMap,
                            sub2VoteMap = sub2MaleVoteMap
                        )
                    }

                    voteBracket.femaleBracketGroups.forEach { group ->
                        parseBracketGroup(
                            group = group,
                            sub1VoteMap = sub1FemaleVoteMap,
                            sub2VoteMap = sub2FemaleVoteMap
                        )
                    }
                }
            }
        }

        val maleRoundWinners = aggregateWinnersIntoBracketGroups(sub1MaleVoteMap, sub2MaleVoteMap, participantMap)
        val femaleRoundWinner = aggregateWinnersIntoBracketGroups(sub1FemaleVoteMap, sub2FemaleVoteMap, participantMap)

        return GenderedRoundWinners(
            maleWinners = maleRoundWinners.sortedBy { it.sortOrder },
            femaleWinners = femaleRoundWinner.sortedBy { it.sortOrder },
            roundNumber = roundNumber
        )
    }

    fun calculateLatestSeasonVoteCountForRound(roundNumber: Int): ServiceResponse<GenderedVoteCountResults> {
        val initialBracketResponse = bracketService.getLatestSeasonBracket()
        val initialBracket = if (initialBracketResponse.isSuccess()) {
            initialBracketResponse.getSuccessValue()
                ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)
        } else {
            val errorResponse = initialBracketResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val latestSeasonNumber = initialBracket.seasonNumber

        val seasonParticipants = participantService.getNoneDtoSeasonsContestants(latestSeasonNumber)

        val votes = if (initialBracket.voteGapTimeMilliseconds != null) {
            voteBracketDao.findBySeasonNumberAndBracketNumber(
                seasonNumber = latestSeasonNumber,
                bracketNumber = roundNumber
            )
        } else {
            voteBracketDao.findBySeasonNumberAndBracketNumberAndFinishedVoting(
                seasonNumber = latestSeasonNumber,
                bracketNumber = roundNumber,
                finishedVoting = true
            )
        }

        return ServiceResponse.successResponse(parseAllVoteCountsForRound(votes, participants = seasonParticipants))
    }

    private fun parseAllVoteCountsForRound(
        votes: List<VoteBracket>,
        participants: Set<Participant>
    ): GenderedVoteCountResults {
        val participantMap = participants.associateBy { it.id }

        val maleVoteCountMap = mutableMapOf<String, Int>()
        val femaleVoteCountMap = mutableMapOf<String, Int>()

        votes.forEach { voteBracket ->
            when (voteBracket) {
                is GenderedVoteBracket -> {
                    voteBracket.maleBracketGroups.forEach { group ->
                        parseBracketGroupForVote(group, maleVoteCountMap)
                    }

                    voteBracket.femaleBracketGroups.forEach { group ->
                        parseBracketGroupForVote(group, femaleVoteCountMap)
                    }
                }
            }
        }

        val maleVoteCountResults = maleVoteCountMap.mapNotNull { (participantId, voteCount) ->
            participantMap[participantId]?.let {
                VoteCountResults(
                    participant = it,
                    voteCount = voteCount
                )
            }
        }

        val femaleVoteResults = femaleVoteCountMap.mapNotNull { (participantId, voteCount) ->
            participantMap[participantId]?.let {
                VoteCountResults(
                    participant = it,
                    voteCount = voteCount
                )
            }
        }

        return GenderedVoteCountResults(
            maleVoteCount = maleVoteCountResults.sortedByDescending { it.voteCount },
            femaleVoteCount = femaleVoteResults.sortedByDescending { it.voteCount }
        )
    }

    private fun aggregateWinnersIntoBracketGroups(
        sub1VoteMap: MutableMap<Int, MutableMap<String, Int>>,
        sub2VoteMap: MutableMap<Int, MutableMap<String, Int>>,
        participantMap: Map<String, Participant>
    ): List<WinningResultsBracketGroup> {
        val sub1Winners = parseWinnerFromAggregatedVotes(sub1VoteMap, participantMap)
        val sub2Winners = parseWinnerFromAggregatedVotes(sub2VoteMap, participantMap)

        return sub1Winners.map { (sortOrder, winner) ->
            val sub2Winner = sub2Winners[sortOrder]

            WinningResultsBracketGroup(
                submissionId1 = winner.first,
                sub1VoteCount = winner.second,
                submissionId2 = sub2Winner?.first,
                sub2VoteCount = sub2Winner?.second ?: 0,
                sortOrder = sortOrder
            )
        }
    }

    private fun parseWinnerFromAggregatedVotes(
        submissionVoteMap: MutableMap<Int, MutableMap<String, Int>>,
        participantMap: Map<String, Participant>
    ): Map<Int, Pair<String, Int>> {
        return submissionVoteMap.map { (groupNumber, participantAndVote) ->
            var winner: Pair<String, Int>? = null

            participantAndVote.forEach { (participantId, voteCount) ->
                winner?.let {
                    if (it.second == voteCount) {
                        val prevParticipant = participantMap[it.first]
                        val currParticipant = participantMap[participantId]

                        if (prevParticipant != null && currParticipant != null) {
                            if (currParticipant.azukiId > prevParticipant.azukiId) {
                                winner = Pair(participantId, voteCount)
                            }
                        } else {
                            LOG.error("One of the participants did not exist: $prevParticipant | $currParticipant")
                        }
                    } else if (it.second < voteCount) {
                        winner = Pair(participantId, voteCount)
                    }
                } ?: run {
                    winner = Pair(participantId, voteCount)
                }
            }

            winner?.let { groupNumber to it } ?: throw RuntimeException("A winner was not chosen...")
        }.toMap()
    }

    private fun parseBracketGroupForVote(
        group: BracketGroup,
        voteCountMap: MutableMap<String, Int>
    ) {
        voteCountMap[group.submissionId1] = voteCountMap.getOrDefault(group.submissionId1, 0) + 1
        group.submissionId2?.let {
            voteCountMap[it] = voteCountMap.getOrDefault(it, 0) + 1
        }
    }

    private fun parseBracketGroup(
        group: BracketGroup,
        sub1VoteMap: MutableMap<Int, MutableMap<String, Int>>,
        sub2VoteMap: MutableMap<Int, MutableMap<String, Int>>
    ) {
        val sub1sortGroup = sub1VoteMap.getOrDefault(group.sortOrder, mutableMapOf())
        sub1sortGroup[group.submissionId1] = sub1sortGroup.getOrDefault(group.submissionId1, 0) + 1
        sub1VoteMap[group.sortOrder] = sub1sortGroup

        val submission2 = group.submissionId2
        if (submission2 !== null) {
            val sub2sortGroup = sub2VoteMap.getOrDefault(group.sortOrder, mutableMapOf())
            sub2sortGroup[group.submissionId2] = sub2sortGroup.getOrDefault(group.submissionId2, 0) + 1
            sub2VoteMap[group.sortOrder] = sub2sortGroup
        }
    }

    fun vote(voteRequestDto: VoteRequestDto): ServiceResponse<VoteBracket> {
        val ip = httpRequestUtil.getClientIpAddressIfServletRequestExist()

        val initialBracketResponse = bracketService.getLatestSeasonBracket()
        val initialBracket = if (initialBracketResponse.isSuccess()) {
            initialBracketResponse.getSuccessValue()
                ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)
        } else {
            val errorResponse = initialBracketResponse as ErrorResponse
            return ServiceResponse.errorResponse(errorResponse.errorCode)
        }

        val latestSeasonNumber = initialBracket.seasonNumber

        // We need their previous bracket (to see if they voted before)
        //  and we need the last round that closed to see if the vote is valid
        val previousUsersVote = voteBracketDao.findByIpAndSeasonNumber(ip, latestSeasonNumber)
            .maxByOrNull { it.bracketNumber }

        val lastClosedRound = fetchNumberOfRounds(initialBracket)

        val lastRoundsWinners = if (lastClosedRound > 0) {
            val resultsResponse = calculateRoundVotesForLatestSeason(
                initialBracket = initialBracket,
                roundNumber = lastClosedRound,
                isRoundByRoundVoting = initialBracket.voteGapTimeMilliseconds != null
            )

           if (resultsResponse.isSuccess()) {
                resultsResponse.getSuccessValue()
                    ?: return ServiceResponse.errorResponse(VoteBracketErrorCodes.WINNER_CALC_ISSUE)
            } else {
                val errorResponse = resultsResponse as ErrorResponse
                return ServiceResponse.errorResponse(errorResponse.errorCode)
            }
        } else null

        return validateVote(
            ip = ip,
            latestSeasonNumber = latestSeasonNumber,
            voteRequestDto = voteRequestDto,
            initialBracket = initialBracket,
            previousRound = lastRoundsWinners,
            previousUsersVote = previousUsersVote
        ) ?: run {
            val voteBracket = GenderedVoteBracket(
                ip = ip,
                twitterHandle = voteRequestDto.twitterHandle,
                seasonNumber = latestSeasonNumber,
                bracketNumber = lastClosedRound + 1,
                maleBracketGroups = voteRequestDto.maleBracketGroups,
                femaleBracketGroups = voteRequestDto.femaleBracketGroups
            )

            voteBracketDao.save(voteBracket)

            // TODO: (initialBracket.numberOfBrackets() - 1) if want to end right before last bracket for Bobu vote
            if (voteBracket.bracketNumber == initialBracket.numberOfBrackets()) {
                updateVotesToFinishedVoting(ip, latestSeasonNumber)
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
        previousRound: GenderedRoundWinners?,
        previousUsersVote: VoteBracket?
    ): ServiceResponse<VoteBracket>? {
        validateVotingDates(
            initialBracket = initialBracket,
            votingRound = (previousRound?.roundNumber ?: 0) + 1
        )?.let { return it }

        validateIfUserFinishedVotingForTheSeason(ip, latestSeasonNumber)?.let { return it }

        validateTwitterHandle(voteRequestDto, previousUsersVote)?.let { return it }

        validateNumberOfGroups(
            voteRequestDto = voteRequestDto,
            initialBracket = initialBracket,
            previousRoundNumber = previousRound?.roundNumber ?: 0,
            previousBracket = previousRound
        )?.let { return it }

        validateIfPastFinalRound(initialBracket, previousRoundNumber = previousRound?.roundNumber ?: 0)?.let {
            return it
        }

        // check if submissions are valid combinations from the previous bracket groups
        // TODO: Later on we need to support different voting types outside of gendered
        val previousBracketNum = previousRound?.roundNumber ?: 0
        val lastVotableBracket = initialBracket.numberOfBrackets()
        val maleBracketGroups = previousRound?.maleWinners?.map { BracketGroup(it) }?.toSet()
            ?: run {
                (initialBracket as GenderedInitialBracket).maleBracketGroups
            }
        validateVotedGroups(
            currentGroup = voteRequestDto.maleBracketGroups,
            previousGroup = maleBracketGroups,
            currentVoteBracketNum = previousBracketNum + 1,
            lastVotableBracketNum = lastVotableBracket
        )?.let { return it }

        val femaleBracketGroups = previousRound?.femaleWinners?.map { BracketGroup(it) }?.toSet()
            ?: run {
                (initialBracket as GenderedInitialBracket).femaleBracketGroups
            }
        validateVotedGroups(
            currentGroup = voteRequestDto.femaleBracketGroups,
            previousGroup = femaleBracketGroups,
            currentVoteBracketNum = previousBracketNum + 1,
            lastVotableBracketNum = lastVotableBracket
        )?.let { return it }

        return null
    }

    private fun validateIfPastFinalRound(
        initialBracket: InitialBracket,
        previousRoundNumber: Int
    ): ServiceResponse<VoteBracket>? {
        // Reject votes that pass the final bracket (for this round final bracket might be second to last)
        val lastVotableBracket = initialBracket.numberOfBrackets()
        // TODO: If we don't want the final vote (for Bobu) then change to ">="
        if ((previousRoundNumber + 1) > lastVotableBracket) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_VOTE_BRACKET)
        }

        return null
    }

    private fun validateNumberOfGroups(
        voteRequestDto: VoteRequestDto,
        initialBracket: InitialBracket,
        previousRoundNumber: Int,
        previousBracket: GenderedRoundWinners?
    ): ServiceResponse<VoteBracket>? {
        // Checks if num of groups are half of the previous round (each round cuts number of groups in half)
        val previousCombinedBracketGroupSize = (previousBracket?.combinedGroup ?: initialBracket.combinedGroups).size
        val currentCombinedBracketGroupSize = (voteRequestDto.maleBracketGroups + voteRequestDto.femaleBracketGroups).size
        val isFinalBracket = (previousRoundNumber + 1) == initialBracket.numberOfBrackets()
        val invalidFinalBracket = (previousRoundNumber + 1) == initialBracket.numberOfBrackets() &&
                previousCombinedBracketGroupSize == 2 &&
                currentCombinedBracketGroupSize != 2

        if (
            (isFinalBracket && invalidFinalBracket) ||
            (!isFinalBracket && currentCombinedBracketGroupSize != previousCombinedBracketGroupSize / 2)
        ) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_NUM_OF_BRACKET_GROUPS)
        }

        return null
    }

    private fun validateTwitterHandle(
        voteRequestDto: VoteRequestDto,
        previousBracket: VoteBracket?
    ): ServiceResponse<VoteBracket>? {
        // Twitter Handle Check
        if (previousBracket == null) {
            // If first vote validate twitter handle is not blank and is a valid username
            // Check if twitterHandle already exists in the db
            if (!voteRequestDto.twitterHandle.isValidTwitterHandle()) {
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

        return null
    }

    private fun validateVotingDates(
        initialBracket: InitialBracket,
        votingRound: Int
    ): ServiceResponse<VoteBracket>? {
        // Check if voting has not started for the season
        if (initialBracket.voteStartDate > timeService.getNow()) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.VOTING_HAS_NOT_STARTED)
        }

        // Check if voting has ended for the season
        if (timeService.getNow() > initialBracket.voteDeadline) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.VOTING_HAS_ENDED)
        }

        // If vote gap exists, validate if user can vote on current round
        initialBracket.voteGapTimeMilliseconds?.let {
            val votingRoundStartDate = initialBracket.roundStartDate(votingRound)
            if (timeService.getNow() < votingRoundStartDate) {
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.VOTING_HAS_NOT_STARTED_FOR_ROUND)
            }
        }

        return null
    }

    /**
     * Validates if user already finished voting for the season, baring them from voting again.
     */
    private fun validateIfUserFinishedVotingForTheSeason(
        ip: String,
        seasonNumber: Int
    ): ServiceResponse<VoteBracket>? {
        val previousVotes = voteBracketDao.findByIpAndSeasonNumber(ip, seasonNumber)

        if (previousVotes.firstOrNull()?.finishedVoting == true) {
            return ServiceResponse.errorResponse(VoteBracketErrorCodes.CANNOT_VOTE_AGAIN)
        }

        return null
    }

    private fun validateVotedGroups(
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
                LOG.error("FirstContestantId $firstContestantId is not valid. Previous vote: $previousGroup")
                return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
            }
        } else {
            currentGroup.toList().sortedBy { it.sortOrder }.forEach { bracketGroup ->
                val firstContestantId = bracketGroup.submissionId1
                val secondContestantId = bracketGroup.submissionId2

                if (!previousGroupPairs[previousGroupIndex].contains(firstContestantId)) {
                    LOG.error("FirstContestantId $firstContestantId is not valid. Previous vote: $previousGroup")
                    return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
                }

                if (!previousGroupPairs[previousGroupIndex + 1].contains(secondContestantId)) {
                    LOG.error("SecondContestantId $secondContestantId is not valid. Previous vote: $previousGroup")
                    return ServiceResponse.errorResponse(VoteBracketErrorCodes.INVALID_GROUP_ORDER)
                }

                previousGroupIndex += 2
            }
        }

        return null
    }
}
