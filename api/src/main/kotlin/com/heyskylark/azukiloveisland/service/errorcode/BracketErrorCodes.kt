package com.heyskylark.azukiloveisland.service.errorcode

enum class BracketErrorCodes(
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    NO_BRACKET_FOUND(
        message = "No bracket could be found.",
        code = "noBracketFound",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_BRACKET_VOTE_DEADLINE(
      message = "Bracket vote deadline must be greater than the current date.",
      code = "invalidBracketVoteDeadline",
      type = ErrorType.BAD_REQUEST
    ),
    BRACKET_ALREADY_EXISTS_FOR_SEASON(
      message = "A bracket already exists for the season.",
      code = "bracketAlreadyExistsForSeason",
      type = ErrorType.BAD_REQUEST
    ),
    NO_CONTESTANTS_TO_BRACKET(
        message = "There are no contestants in this season so a bracket cannot be created.",
        code = "noContestantsToBracket",
        type = ErrorType.BAD_REQUEST
    ),
    UN_EVEN_NUM_CONTESTANTS(
        message = "There is an un-even number of contestants, so proper bracket groups cannot be created.",
        code = "unEvenNumContestants",
        type = ErrorType.BAD_REQUEST
    ),
    NOT_ENOUGH_CONTESTANTS(
        message = "There must be at least four contestants before a bracket is created.",
        code = "notEnoughContestants",
        type = ErrorType.BAD_REQUEST
    ),
    UNDETERMINED_CONTESTANT_GENDER(
        message = "Contestant has undetermined gender, so male/female bracket split cannot be created.",
        code = "undeterminedContestantGender",
        type = ErrorType.BAD_REQUEST
    ),
    UN_EVEN_GENDER_SPLIT(
        message = "Un-even split between male/female bracket contestants.",
        code = "unEvenGenderSplit",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_BRACKET_TYPE(
        message = "That bracket type does not exist.",
        code = "invalidBracketType",
        type = ErrorType.BAD_REQUEST
    )
    ;
}