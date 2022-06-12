package com.heyskylark.azukiloveisland.service

import com.heyskylark.azukiloveisland.dto.VoteDto
import com.heyskylark.azukiloveisland.model.VoteRecord
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import org.springframework.stereotype.Component

@Component("voteService")
class VoteService : BaseService() {
//    fun vote(
//        bracketId: String,
//        groupNumber: Int,
//        voteDto: VoteDto
//    ): ServiceResponse<Vote> {
//        validateVote(
//            bracketId = bracketId,
//            groupNumber = groupNumber,
//            voteDto = voteDto
//        ) ?: run {
//            // send queue event voteProducer.vote(bracketId, groupNumber, participantId, userId)
//
//            return ServiceResponse.successResponse()
//        }
//    }
//
//    private fun validateVote(
//        bracketId: String,
//        groupNumber: Int,
//        voteDto: VoteDto
//    ): ServiceResponse<Vote>? {
//        // Check if bracket exists val bracket = bracketDao[bracketId] ?: return ServiceResponse.errorResponse(...)
//        // Check if group exists in the bracket...
//        // Validate if user already voted on this bracket/group
//        //      (VoteRecord table if exists [from: userHash to: ${bracket.id}.$groupNumber])
//        // Check if vote participant exists in the group...
//
//        return null
//    }
//
//    fun getVotes(bracketId: String, userId: String): ServiceResponse<Set<VoteRecord>> {
//        voteRecordDao.getVotes(bracketId, userId)
//    }
}