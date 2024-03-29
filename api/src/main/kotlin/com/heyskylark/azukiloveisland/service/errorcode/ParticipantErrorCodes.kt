package com.heyskylark.azukiloveisland.service.errorcode

enum class ParticipantErrorCodes(
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    SEASON_CONTESTANTS_NOT_FOUND(
        message = "Cannot find contestants for the season.",
        code = "seasonContestantsNotFound",
        type = ErrorType.BAD_REQUEST
    ),
    PARTICIPANT_TWITTER_NOT_FOUND(
        message = "Participant with given twitter handle could not be found.",
        code = "participantTwitterNotFound",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_TWITTER_HANDLE(
        message = "Twitter handle is invalid.",
        code = "invalidTwitterHandle",
        type = ErrorType.BAD_REQUEST
    ),
    SEASON_SUBMISSIONS_ARE_NOT_ACTIVE(
        message = "Submissions are not currently active.",
        code = "seasonParticipationIsNotActive",
        type = ErrorType.BAD_REQUEST
    ),
    AZUKI_INFO_MISSING(
        message = "Azuki info was not found in the success response",
        code = "azukiInfoMissing",
        type = ErrorType.INTERNAL_SERVER_ERROR
    ),
    AZUKI_ID_ALREADY_EXISTS(
        message = "Participant with this Azuki ID already exists for the season.",
        code = "azukiIdAlreadyExists",
        type = ErrorType.BAD_REQUEST
    ),
    OWNER_ADDRESS_EXISTS(
        message = "Participant with this owner address already exists for the season.",
        code = "ownerAddressExists",
        type = ErrorType.BAD_REQUEST
    ),
    TWITTER_HANDLE_EXISTS(
        message = "Participant with this Twitter handle already exists for the season.",
        code = "twitterHandleExists",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_QUOTE_ERROR(
        message = "Quote must be present and cannot be longer than 100 characters.",
        code = "invalidQuoteError",
        type = ErrorType.BAD_REQUEST
    ),
    BIO_TOO_LONG_ERROR(
        message = "Bio must not be longer than 200 characters.",
        code = "bioTooLongError",
        type = ErrorType.BAD_REQUEST
    ),
    HOBBIES_TOO_LONG_ERROR(
        message = "Cannot include more than 5 hobbies.",
        code = "hobbiesTooLongError",
        type = ErrorType.BAD_REQUEST
    )
    ;
}