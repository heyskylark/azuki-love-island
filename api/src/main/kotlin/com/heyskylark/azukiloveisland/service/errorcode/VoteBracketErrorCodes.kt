package com.heyskylark.azukiloveisland.service.errorcode

enum class VoteBracketErrorCodes(
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    CANNOT_VOTE_AGAIN(
        message = "You cannot vote again on this season.",
        code = "cannotVoteAgain",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_VOTE_BRACKET_NUMBER(
        message = "Cannot vote on a bracket that is in the past or future.",
        code = "invalidVoteBracketNumber",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_NUM_OF_BRACKET_GROUPS(
        message = "Number of bracket groups should be half of the previous bracket",
        code = "invalidNumOfBracketGroups",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_VOTE_BRACKET(
        message = "You've exceeded the final bracket you can vote on.",
        code = "invalidVoteBracket",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_GROUP_ORDER(
        message = "Bracket group in vote is not valid",
        code = "invalidGroupOrder",
        type = ErrorType.BAD_REQUEST
    )
    ;
}
