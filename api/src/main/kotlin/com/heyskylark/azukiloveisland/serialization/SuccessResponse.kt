package com.heyskylark.azukiloveisland.serialization

class SuccessResponse<O>(private val value: O? = null) : ServiceResponse<O>() {
    override fun getSuccessValue(): O? = value

    override fun success(successCallback: (O?) -> Unit): ServiceResponse<O> {
        successCallback.invoke(value)
        return this
    }
}