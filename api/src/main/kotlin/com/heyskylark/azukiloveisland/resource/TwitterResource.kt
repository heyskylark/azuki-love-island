package com.heyskylark.azukiloveisland.resource

import com.heyskylark.azukiloveisland.client.TwitterClient
import com.heyskylark.azukiloveisland.serialization.ResponseBuilder
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
        return ResponseBuilder.buildResponse(twitterClient.getUsersLoveIslandTweet(handle))
    }
}