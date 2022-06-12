package com.heyskylark.azukiloveisland.serialization

import com.heyskylark.azukiloveisland.dto.HttpErrorDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

class ResponseBuilder {
    companion object {
        fun <O> buildResponse(serviceResponse: ServiceResponse<O>): ResponseEntity<String> {
            return buildResponse(serviceResponse) { buildError(it) }
        }

        private fun <O> buildResponse(
            serviceResponse: ServiceResponse<O>,
            errorTransformer: (ErrorResponse<O>) -> ResponseEntity<String>
        ): ResponseEntity<String> {
            lateinit var response: ResponseEntity<String>
            serviceResponse
                .success { obj ->
                    response = ResponseEntity.ok(
                        (obj ?: emptyMap<Unit, Unit>()).let { JSONSerializer.serialize(it) }
                    )
                }
                .error { response = errorTransformer(it) }

            return response
        }

        private fun <O> buildError(errorResponse: ErrorResponse<O>): ResponseEntity<String> {
            val errorCode = errorResponse.errorCode
            val httpErrorDto = HttpErrorDto(errorCode)

            return ResponseEntity
                .status(errorCode.type.value)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JSONSerializer.serialize(httpErrorDto))
        }
    }
}
