package com.heyskylark.azukiloveisland.service.errorcode

enum class SeasonErrorCodes(
    override val message: String,
    override val code: String,
    override val type: ErrorType
) : ErrorCode {
    NO_SEASONS_FOUND(
        message = "Could not find any seasons.",
        code = "noSeasonsFound",
        type = ErrorType.NOT_FOUND
    ),
    SEASON_DOES_NOT_EXIST(
        message = "That season does not exist.",
        code = "seasonDoesNotExist",
        type = ErrorType.NOT_FOUND
    )
    ;
}