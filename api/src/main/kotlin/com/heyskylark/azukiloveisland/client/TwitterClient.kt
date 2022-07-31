package com.heyskylark.azukiloveisland.client

import com.heyskylark.azukiloveisland.serialization.ServiceResponse
import com.heyskylark.azukiloveisland.service.BaseService
import com.heyskylark.azukiloveisland.service.errorcode.BaseErrorCode
import com.heyskylark.azukiloveisland.service.errorcode.ErrorCode
import com.heyskylark.azukiloveisland.service.errorcode.ErrorType
import com.twitter.clientlib.ApiException
import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.model.Tweet
import com.twitter.clientlib.model.User
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.springframework.stereotype.Component

@Component("twitterClient")
class TwitterClient(
    private val twitterApi: TwitterApi
): BaseService() {
    fun getUsersId(
        twitterHandle: String,
        expansions: Set<String>? = null,
        tweetFields: Set<String>? = null,
        userFields: Set<String>? = null
    ): ServiceResponse<User?> {
        try {
            val userLookupResponse = twitterApi
                .users()
                .findUserByUsername(twitterHandle, expansions, tweetFields, userFields)

            return ServiceResponse.successResponse(userLookupResponse.data)
        } catch (e: ApiException) {
            LOG.error("There was a problem fetching $twitterHandle twitter ID.")
            
            return ServiceResponse.errorResponse(
                BaseErrorCode(
                    code = "twitterUserLookupError",
                    message = e.message ?: "There was a problem looking up the twitter user $twitterHandle",
                    type = ErrorType.fromCode(e.code) ?: ErrorType.BAD_REQUEST
                )
            )
        }
    }

    fun getLoveIslandTweets(
        query: String,
        tweetLimit: Int = 10,
        startTime: OffsetDateTime? = null,
        endTime: OffsetDateTime? = null
    ): ServiceResponse<List<Tweet>> {
        try {
            val searchResponse = twitterApi.tweets().tweetsRecentSearch(
                query,
                startTime,
                endTime,
                null,
                null,
                tweetLimit,
                null,
                null,
                null,
                setOf("attachments.media_keys"),
                setOf("attachments", "created_at"),
                null,
                setOf("preview_image_url", "url"),
                null,
                null
            )

            return ServiceResponse.successResponse(
                searchResponse.data?.mapNotNull { it } ?: emptyList()
            )
        } catch (e: ApiException) {
            LOG.error("There was a problem fetching Azuki Love Island tweets.", e)

            return ServiceResponse.errorResponse(
                BaseErrorCode(
                    code = "twitterUserLookupError",
                    message = e.responseBody ?: "There was a problem fetching Azuki Love Island tweets.",
                    type = ErrorType.fromCode(e.code) ?: ErrorType.BAD_REQUEST
                )
            )
        }
    }
}