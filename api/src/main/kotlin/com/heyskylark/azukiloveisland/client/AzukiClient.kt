package com.heyskylark.azukiloveisland.client

import com.heyskylark.azukiloveisland.model.azuki.AzukiMetadata
import com.heyskylark.azukiloveisland.serialization.JSONSerializer
import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.errorcode.ErrorCode
import com.heyskylark.azukiloveisland.service.errorcode.ErrorType
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component

@Component("azukiClient")
class AzukiClient(private val httpClient: CloseableHttpClient) {
    companion object {
        val LOG: Logger = LogManager.getLogger(AzukiClient::class)
        const val BASE_URL = "https://ikzttp.mypinata.cloud/ipfs/QmQFkLSQysj94s5GvTHPyzTxrawwtjgiiYS2TBLgrvw8CW/"
    }

    fun getMetadata(azukiId: Long): ServiceResponse<AzukiMetadata> {
        val url = BASE_URL + azukiId
        val httpGet = HttpGet(url)

        return try {
            val response = httpClient.execute(httpGet)
            val statusCode = response.statusLine.statusCode

            return if (statusCode in 200..299) {
                val azukiMetadata = JSONSerializer.deserialize(
                    response.entity.content,
                    AzukiMetadata::class.java
                )

                ServiceResponse.successResponse(azukiMetadata)
            } else {
                LOG.error("The azuki metadata request responded with an error: $statusCode | ${response.entity.content}")
                ServiceResponse.errorResponse(
                    object : ErrorCode {
                        override val message = "Bad response from Azuki IPFS"
                        override val code = "azukiClientError"
                        override val type = ErrorType.fromCode(statusCode) ?: ErrorType.INTERNAL_SERVER_ERROR
                    }
                )
            }
        } catch (e: Exception) {
            LOG.error("There was a problem fetching Azuki #$azukiId metadata", e)
            ServiceResponse.errorResponse(AzukiClientErrorCode.CLIENT_ERROR)
        }
    }
}