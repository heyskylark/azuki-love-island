package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.resource.dao.SeasonsDao
import com.heyskylark.azukiloveisland.model.season.Season
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.SeasonErrorCodes
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component("seasonService")
class SeasonService(
    private val seasonsDao: SeasonsDao
) {
    fun getRawSeason(seasonNumber: Int): Season? {
        return seasonsDao.findByIdOrNull(seasonNumber)
    }

    fun getLatestSeason(): ServiceResponse<Season> {
        val latestSeason = getRawLatestSeason()
            ?: return ServiceResponse.errorResponse(SeasonErrorCodes.NO_SEASONS_FOUND)

        return ServiceResponse.successResponse(latestSeason)
    }

    fun getRawLatestSeason(): Season? {
        return seasonsDao.findFirstByOrderBySeasonNumberDesc()
    }

    fun createNewSeason(): Season {
        val latestSeason = getRawLatestSeason()
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