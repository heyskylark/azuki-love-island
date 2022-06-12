package com.heyskylark.azukiloveisland.dao

import com.heyskylark.azukiloveisland.model.season.Season
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("seasonRelationshipsDao")
interface SeasonsDao : CrudRepository<Season, Int> {
    fun findFirstByOrderBySeasonNumberDesc(): Season?
}