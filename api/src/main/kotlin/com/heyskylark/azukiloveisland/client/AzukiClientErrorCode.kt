package com.heyskylark.azukiloveisland.client

import com.heyskylark.azukiloveisland.service.errorcode.ErrorCode
import com.heyskylark.azukiloveisland.service.errorcode.ErrorType

enum class AzukiClientErrorCode(
    override val code: String,
    override val message: String,
    override val type: ErrorType
) : ErrorCode {
    CLIENT_ERROR(
        code = "clientError",
        message = "There was a problem fetching the Azuki metadata",
        type = ErrorType.INTERNAL_SERVER_ERROR
    )
    ;
}