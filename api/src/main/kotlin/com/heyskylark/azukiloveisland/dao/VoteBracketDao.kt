package com.heyskylark.azukiloveisland.dao

import com.heyskylark.azukiloveisland.model.voting.VoteBracket
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("VoteBracketDao")
interface VoteBracketDao : CrudRepository<VoteBracket, String> {
    fun findByTwitterHandle(twitterHandle: String): List<VoteBracket>

    fun findByIpAndSeasonNumber(ip: String, seasonNumber: Int): List<VoteBracket>

    fun findBySeasonNumberAndFinishedVoting(seasonNumber: Int, finishedVoting: Boolean): Set<VoteBracket>
}
