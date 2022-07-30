package com.heyskylark.azukiloveisland.service.errorcode

enum class VoteBracketErrorCodes(
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    WINNER_CALC_ISSUE(
        message = "There was an issue calculating the winners for the round.",
        code = "winnerCalcIssue",
        type = ErrorType.INTERNAL_SERVER_ERROR
    ),
    TWITTER_HANDLE_USED(
        message = "The twitter handle given has already been used to vote this round.",
        code = "twitterHandleUsed",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_TWITTER_HANDLE(
        message = "The twitter handle you have given is invalid.",
        code = "invalidTwitterHandle",
        type = ErrorType.BAD_REQUEST
    ),
    VOTING_HAS_NOT_STARTED(
        message = "Voting for the season has not started yet.",
        code = "votingHasNotStarted",
        type = ErrorType.BAD_REQUEST
    ),
    VOTING_HAS_NOT_STARTED_FOR_ROUND(
        message = "Voting has not started for this round yet.",
        code = "votingHasNotStartedForRound",
        type = ErrorType.BAD_REQUEST
    ),
    VOTING_HAS_ENDED(
        message = "Voting has ended for this season.",
        code = "votingHasEnded",
        type = ErrorType.BAD_REQUEST
    ),
    NO_VOTE_BRACKET_FOUND(
        message = "No votes have been found for this user.",
        code = "noVoteBracketFound",
        type = ErrorType.NOT_FOUND
    ),
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
