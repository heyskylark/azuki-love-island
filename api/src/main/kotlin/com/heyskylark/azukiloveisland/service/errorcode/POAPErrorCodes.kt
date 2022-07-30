package com.heyskylark.azukiloveisland.service.errorcode

enum class POAPErrorCodes (
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    PROBLEM_CREATING_POAP_CLAIM(
        code = "problemCreatingPoapClaim",
        message = "There was a problem saving the POAP claim.",
        type = ErrorType.INTERNAL_SERVER_ERROR
    ),
    PROBLEM_CLAIMING_POAP(
        code = "problemClaimingPoap",
        message = "There was a problem claiming and updating the POAP claim.",
        type = ErrorType.INTERNAL_SERVER_ERROR
    ),
    POAP_EXIST_FOR_SEASON(
      code = "poapExistForSeason",
      message = "POAPs have already been loaded for this season.",
      type = ErrorType.BAD_REQUEST
    ),
    SEASON_POAP_NOT_FOUND(
        code = "seasonPoapNotFound",
        message = "No claimable POAPs could be found for this season.",
        type = ErrorType.NOT_FOUND
    ),
    POAP_CLAIM_WINDOW_NOT_OPENED_YET(
        code = "poapClaimWindowNotOpenedYet",
        message = "The POAP you are trying to claim is not claimable yet.",
        type = ErrorType.BAD_REQUEST
    ),
    POAP_CLAIM_WINDOW_CLOSED(
        code = "poapClaimWindowClosed",
        message = "We're sorry, the POAP you are trying to claim is now closed.",
        type = ErrorType.BAD_REQUEST
    ),
    NO_CLAIMABLE_POAPS_AVAILABLE(
        code = "noClaimablePoapsAvailable",
        message = "We're sorry, there are no more POAPs that are available to claim.",
        type = ErrorType.BAD_REQUEST
    ),
    POAP_ALREADY_CLAIMED_BY_IP(
        code = "poapAlreadyClaimedByIp",
        message = "A POAP has already been claimed on this IP address.",
        type = ErrorType.BAD_REQUEST
    ),
    USER_ALREADY_CLAIMED_POAP(
        code = "userAlreadyClaimedPoap",
        message = "This user has already claimed a POAP.",
        type = ErrorType.BAD_REQUEST
    ),
    USER_IS_NOT_VALID_TO_CLAIM(
        code = "userIsNotValidToClaim",
        message = "This user cannot claim a POAP, a user must have voted to claim a POAP.",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_CLAIM_WINDOWS(
        code = "invalidClaimWindows",
        message = "Claim start window must be before the claim end window.",
        type = ErrorType.BAD_REQUEST
    ),
    INVALID_CLAIM_END_WINDOW(
        code = "invalidClaimEndWindow",
        message = "Claim end window must be in the future.",
        type = ErrorType.BAD_REQUEST
    )
    ;
}
