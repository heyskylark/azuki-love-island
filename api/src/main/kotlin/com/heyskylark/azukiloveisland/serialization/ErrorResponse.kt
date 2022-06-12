package com.heyskylark.azukiloveisland.serialization

import com.heyskylark.azukiloveisland.service.errorcode.ErrorCode

class ErrorResponse<O>(
    val errorCode: ErrorCode
) : ServiceResponse<O>() {
    override fun getSuccessValue(): O? = null

    override fun error(errorCallback: (ErrorResponse<O>) -> Unit): ServiceResponse<O> {
        errorCallback.invoke(this)
        return this
    }
}