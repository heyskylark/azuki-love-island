package com.heyskylark.azukiloveisland.dto

import com.heyskylark.azukiloveisland.service.errorcode.ErrorCode

data class HttpErrorDto(
    val message: String,
    val code: String,
    val type: Int
) {
    constructor(errorCode: ErrorCode) : this(
        message = errorCode.message,
        code = errorCode.code,
        type = errorCode.type.value
    )
}
