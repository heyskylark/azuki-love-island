package com.heyskylark.azukiloveisland.service.errorcode

interface ErrorCode {
    val message: String
    val code: String
    val type: ErrorType
}

enum class ErrorType(val value: Int) {
    RESET_CONTENT(205),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500)
    ;

    companion object {
        private val CODE_LOOKUP = values().associateBy(ErrorType::value)

        fun fromCode(code: Int): ErrorType? {
            return CODE_LOOKUP[code]
        }
    }
}
