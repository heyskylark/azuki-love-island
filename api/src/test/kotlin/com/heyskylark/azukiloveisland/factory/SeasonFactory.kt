package com.heyskylark.azukiloveisland.factory

import com.heyskylark.azukiloveisland.model.season.Season

class SeasonFactory {
    companion object {
        const val DEFAULT_SEASON_NUMBER = 1

        fun createSeason(submissionActive: Boolean): Season {
            return Season(
                seasonNumber = 1,
                submissionActive = submissionActive
            )
        }
    }
}