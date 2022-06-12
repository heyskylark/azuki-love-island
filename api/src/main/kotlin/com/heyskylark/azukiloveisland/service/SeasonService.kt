package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dao.SeasonsDao
import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.season.Season
import org.springframework.stereotype.Component

@Component("seasonService")
class SeasonService(
    private val seasonsDao: SeasonsDao
) {
    fun getSeason(seasonNumber: Int): Season? {
        return seasonsDao.findById(seasonNumber).orElse(null)
    }

    fun getLatestSeason(): Season? {
        return seasonsDao.findFirstByOrderBySeasonNumberDesc()
    }

    fun saveSeason(season: Season) {
        seasonsDao.save(season)
    }

    fun createNewSeason(): Season {
        val latestSeason = getLatestSeason()
        latestSeason?.let { deactivateSeasonSubmissions(it) }

        val newSeasonNumber = (latestSeason?.seasonNumber ?: 0) + 1
        val season = Season(seasonNumber = newSeasonNumber)

        seasonsDao.save(season)

        return season
    }

    fun deactivateSeasonSubmissions(season: Season) {
        val deactivatedSeason = season.copy(
            submissionActive = false
        )

        seasonsDao.save(deactivatedSeason)
    }
}