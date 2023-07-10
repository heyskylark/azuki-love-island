package com.heyskylark.azukiloveisland.resource.dao

import com.heyskylark.azukiloveisland.model.voting.InitialBracket
import java.time.Instant
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("initialBracketDao")
interface InitialBracketDao : CrudRepository<InitialBracket, String> {
    fun findBySeasonNumber(seasonNumber: Int): InitialBracket?

    fun findFirstByOrderBySeasonNumberDesc(): InitialBracket?

    fun findFirstByVoteStartDateGreaterThanEqualOrderBySeasonNumberDesc(
        voteStartDate: Instant
    ): InitialBracket?
}
