package com.heyskylark.azukiloveisland.dao

import com.heyskylark.azukiloveisland.model.voting.InitialBracket
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("initialBracketDao")
interface InitialBracketDao : CrudRepository<InitialBracket, String> {
    fun findBySeasonNumber(seasonNumber: Int): InitialBracket?
}
