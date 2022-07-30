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
import org.springframework.stereotype.Component

@Component("twitterClient")
class TwitterClient(
    private val twitterApi: TwitterApi
): BaseService() {
    companion object {
        val HASHTAGS = listOf(
            "#AzukiLoveIsland",
            "#AzukiLoveIsland2"
        )
    }

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

    fun getUsersLoveIslandTweet(
        twitterHandle: String
    ): ServiceResponse<List<Tweet>> {
        val queryBuilder = StringBuilder()
        HASHTAGS.forEachIndexed {index, hashtag ->
            if (index < HASHTAGS.size - 1) {
                queryBuilder.append("$hashtag OR ")
            } else {
                queryBuilder.append(hashtag)
            }
        }

        queryBuilder.append(" from:$twitterHandle")

        try {
            val testQuery = "(#AzukiLoveIsland OR #AzukiLoveIsland2) -is:retweet -is:reply from:$twitterHandle"
            val searchResponse = twitterApi.tweets().tweetsRecentSearch(
                testQuery,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                setOf("attachments.media_keys"),
                setOf("attachments"),
                null,
                setOf("preview_image_url", "url"),
                null,
                null
            )

            return ServiceResponse.successResponse(
                searchResponse.data?.mapNotNull { it } ?: emptyList()
            )
        } catch (e: ApiException) {
            LOG.error("There was a problem fetching $twitterHandle Azuki Love Island Tweets.", e)

            return ServiceResponse.errorResponse(
                BaseErrorCode(
                    code = "twitterUserLookupError",
                    message = e.responseBody ?: "There was a problem looking up the twitter user $twitterHandle",
                    type = ErrorType.fromCode(e.code) ?: ErrorType.BAD_REQUEST
                )
            )
        }
    }
}