package com.heyskylark.azukiloveisland.config.dao

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration

@Configuration
class MongoDbClientConfig(
    @Value("\${mongodb.db}")
    private val database: String,
    @Value("\${mongodb.ip}")
    private val ip: String,
    @Value("\${mongodb.port}")
    private val port: String,
    @Value("\${mongodb.username}")
    private val username: String,
    @Value("\${mongodb.password}")
    private val password: String,
    @Value("\${mongodb.uri}")
    private val mongoDbUri: String
) : AbstractMongoClientConfiguration() {
    override fun getDatabaseName(): String {
        return database
    }

    override fun mongoClient(): MongoClient {
        val connectionStringBuilder = StringBuilder("mongodb://")
        if (username.isNotBlank() && password.isNotBlank()) {
            connectionStringBuilder.append("$username:$password@")
        }
        connectionStringBuilder.append("$ip:$port/$database")

        val connectionString = ConnectionString(connectionStringBuilder.toString())
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()

        return MongoClients.create(mongoClientSettings)
    }

    override fun getMappingBasePackages(): Collection<String> {
        return setOf("com.heyskylark.azukiloveisland")
    }
}
