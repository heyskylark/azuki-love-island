package com.heyskylark.azukiloveisland.serialization

import com.heyskylark.azukiloveisland.service.errorcode.ErrorCode

abstract class ServiceResponse<O> {
    companion object {
        fun <O> successResponse(value: O? = null): ServiceResponse<O> {
            return SuccessResponse(value)
        }

        fun <O> errorResponse(errorCode: ErrorCode): ServiceResponse<O> {
            return ErrorResponse(errorCode)
        }
    }

    fun isSuccess(): Boolean {
        return this is SuccessResponse
    }

    abstract fun getSuccessValue(): O?

    open fun success(successCallback: (O?) -> Unit): ServiceResponse<O> {
        return this
    }

    open fun error(errorCallback: (ErrorResponse<O>) -> Unit): ServiceResponse<O> {
        return this
    }
}