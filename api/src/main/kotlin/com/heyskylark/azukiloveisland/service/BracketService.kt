package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.model.Bracket
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import org.springframework.stereotype.Component

@Component("bracketService")
class BracketService : BaseService() {
//    fun getBracket(bracketId: String): ServiceResponse<Bracket> {
//        return ServiceResponse.successResponse(bracketDao[bracketId])
//    }
//
//    fun getSeasonBrackets(seasonNumber: Int): ServiceResponse<Set<Bracket>> {
//        val brackets = bracketDao.getBracketsBySeason(seasonNumber)
//
//        return ServiceResponse.successResponse(brackets)
//    }
//
//    fun createBracket(bracketDto: BracketDto): ServiceResponse<Bracket> {
//        validateBracketCreate(bracketDto) ?: run {
//            val bracket = Bracket()
//
//            bracketDao.put(bracket)
//
//            return ServiceResponse.successResponse(bracket)
//        }
//    }
//
//    private fun validateBracketCreate(bracketDto: BracketDto): ServiceResponse<Bracket>? {
//        // Validate if a bracket is already in progress
//        // Validate if start and end time are valid start in the future, and end time bigger than start
//
//        return null
//    }
}
