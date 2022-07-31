package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.client.TwitterClient
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
import java.time.Instant
import java.time.ZoneOffset
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    value = ["/twitter"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class TwitterResource(
    private val twitterClient: TwitterClient
) {
    @GetMapping("/island")
    fun getLoveIslandTweets(
        @RequestParam("handle", required = true) handle: String
    ): ResponseEntity<String> {
        val query = "(#AzukiLoveIsland OR #AzukiLoveIsland2) -is:retweet -is:reply from:$handle"
        return ResponseBuilder.buildResponse(twitterClient.getLoveIslandTweets(query = query, tweetLimit = 1))
    }

    @GetMapping("/island/drama")
    fun getDailyDrama(
        @RequestParam("startTime", required = false) startTimeMilli: Long? = null,
        @RequestParam("endTime", required = false) endTimeMilli: Long? = null
    ): ResponseEntity<String> {
        val query = "(#AzukiLoveIsland OR #AzukiLoveIsland2) -is:retweet -is:reply"
        val startTime = startTimeMilli?.let {
            Instant.ofEpochMilli(startTimeMilli).atOffset(ZoneOffset.UTC)
        }

        val endTime = endTimeMilli?.let {
            Instant.ofEpochMilli(endTimeMilli).atOffset(ZoneOffset.UTC)
        }

        return ResponseBuilder.buildResponse(
            twitterClient.getLoveIslandTweets(query = query, tweetLimit = 10, startTime = startTime, endTime = endTime)
        )
    }
}