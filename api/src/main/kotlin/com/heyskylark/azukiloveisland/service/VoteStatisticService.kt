package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.resource.dao.InitialBracketDao
import com.heyskylark.azukiloveisland.resource.dao.ParticipantDao
import com.heyskylark.azukiloveisland.resource.dao.VoteBracketDao
import com.heyskylark.azukiloveisland.model.azuki.Gender
import com.heyskylark.azukiloveisland.model.voting.GenderedVoteBracket
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.BracketErrorCodes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import org.springframework.stereotype.Component

@Component("voteStatisticService")
class VoteStatisticService(
    private val voteBracketDao: VoteBracketDao,
    private val initialBracketDao: InitialBracketDao,
    private val participantDao: ParticipantDao
) {
    companion object {
        const val ROUND_DIVISIONS = 6
        const val ONE_DAY_MILLI = 86400000L
    }

    fun generateSeasonVotStats(
        seasonNumber: Int,
        gender: Gender,
        roundDivisions: Int? = null,
    ): ServiceResponse<String> {
        val participants = participantDao.findBySeasonNumberAndSubmitted(seasonNumber, submitted = true)
            .filter { it.gender == gender }

        val idToTwitterHandle = participants.associate { it.id to it.twitterHandle }

        val totalVotes = participants.associate { it.id to mutableListOf<Long>() }.toMutableMap()

        val initBracket = initialBracketDao.findBySeasonNumber(seasonNumber)
            ?: return ServiceResponse.errorResponse(BracketErrorCodes.NO_BRACKET_FOUND)

        val stringBuilder = StringBuilder()
        val timeDivisions = getTimeDivisions(
            start = initBracket.voteStartDate,
            deadline = initBracket.voteDeadline,
            roundDivisions = roundDivisions ?: ROUND_DIVISIONS,
            roundDurationMilli = initBracket.voteGapTimeMilliseconds ?: ONE_DAY_MILLI
        ).toMutableList()

        stringBuilder.append(generateHeaders(timeDivisions))
        stringBuilder.appendLine()

        val sortedVotes = voteBracketDao.findBySeasonNumber(seasonNumber)
            .map { it as GenderedVoteBracket }
            .sortedBy { it.createdAt }

        var currentTime = timeDivisions.removeFirst()
        val currentVoteCount = participants.associate { it.id to 0L }.toMutableMap()
        sortedVotes.forEach { vote ->
            while (vote.createdAt > currentTime) {
                currentVoteCount.forEach { (id, voteCount) ->
                    val oldList = totalVotes[id]!!
                    oldList.add(voteCount)
                    totalVotes[id] = oldList
                }

                // Technically we should never worry about reaching the end of the list cause votes shouldn't happen after deadline
                currentTime = timeDivisions.removeFirst()
            }

            // Add votes
            if (gender == Gender.MALE) {
                vote.maleBracketGroups
            } else {
                vote.femaleBracketGroups
            }.forEach { bracketGroup ->
                currentVoteCount[bracketGroup.submissionId1] = currentVoteCount[bracketGroup.submissionId1]!! + 1
                bracketGroup.submissionId2?.let {
                    currentVoteCount[it] = currentVoteCount[it]!! + 1
                }
            }
        }

        currentVoteCount.forEach { (id, voteCount) ->
            val oldList = totalVotes[id]!!
            oldList.add(voteCount)
            totalVotes[id] = oldList
        }

        // Parse out calculated votes
        totalVotes.forEach { (id, votesList) ->
            val twitterHandle = idToTwitterHandle[id]!!
            val sb = StringBuilder(twitterHandle)
            votesList.forEach { voteCount ->
                sb.append(",$voteCount")
            }

            stringBuilder.append(sb.toString())
            stringBuilder.appendLine()
        }

        return ServiceResponse.successResponse(stringBuilder.toString())
    }

    // From start date
    // How long each round lasts
    // How much you want to divide each round
    private fun getTimeDivisions(
        start: Instant,
        deadline: Instant,
        roundDivisions: Int,
        roundDurationMilli: Long
    ): List<Instant> {
        val timeDivisions = mutableListOf<Instant>()

        val nextTimeMilli = roundDurationMilli / roundDivisions
        var currentTime = start

        while (currentTime <= deadline) {
            timeDivisions.add(currentTime)
            currentTime = currentTime.plusMillis(nextTimeMilli)
        }

        return timeDivisions
    }

    private fun generateHeaders(timeDivisions: List<Instant>): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
            .withLocale( Locale.US )
            .withZone(ZoneId.of("UTC"))

        val stringBuilder = StringBuilder("handle")
        timeDivisions.forEach {
            stringBuilder.append(",${formatter.format(it).replace(",", "")}")
        }

        return stringBuilder.toString()
    }
}