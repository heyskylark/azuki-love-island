package com.heyskylark.azukiloveisland.config.web3

import java.net.URI
import java.net.URL
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.websocket.WebSocketClient
import org.web3j.protocol.websocket.WebSocketService

@Configuration
class Web3jConfig(
    @Value("\${web3.ethereum.rpc}")
    private val ethereumRpcEndpoint: URL,
    @Value("\${web3.ethereum.rpc}")
    private val ethereumWsEndpoint: URI
) {
    @Bean("web3j")
    fun web3j(): Web3j {
        val httpService = HttpService(ethereumRpcEndpoint.toString())
        return Web3j.build(httpService)
    }

    @Bean("websocketService")
    fun websocketService(): WebSocketService {
        val client = WebSocketClient(ethereumWsEndpoint)
        return WebSocketService(client, false)
    }
}
