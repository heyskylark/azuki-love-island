package com.heyskylark.azukiloveisland.dao

import com.heyskylark.azukiloveisland.model.voting.VoteBracket
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("VoteBracketDao")
interface VoteBracketDao : CrudRepository<VoteBracket, String> {
    fun findByTwitterHandleAndSeasonNumber(twitterHandle: String, seasonNumber: Int): List<VoteBracket>

    fun findBySeasonNumber(seasonNumber: Int): List<VoteBracket>

    fun findBySeasonNumberAndBracketNumberAndTwitterHandleIgnoreCase(
        seasonNumber: Int,
        bracketNumber: Int,
        twitterHandle: String
    ): VoteBracket?

    fun findBySeasonNumberAndTwitterHandleIgnoreCase(
        seasonNumber: Int,
        twitterHandle: String
    ): List<VoteBracket>

    fun findByIpAndSeasonNumberAndBracketNumber(
        ip: String,
        seasonNumber: Int,
        bracketNumber: Int
    ): List<VoteBracket>

    fun findByIpAndSeasonNumber(ip: String, seasonNumber: Int): List<VoteBracket>

    fun findBySeasonNumberAndBracketNumber(
        seasonNumber: Int,
        bracketNumber: Int
    ): List<VoteBracket>

    fun findBySeasonNumberAndBracketNumberAndFinishedVoting(
        seasonNumber: Int,
        bracketNumber: Int,
        finishedVoting: Boolean
    ): List<VoteBracket>
}
