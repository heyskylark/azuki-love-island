package com.heyskylark.azukiloveisland.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.heyskylark.azukiloveisland.exception.JsonSerializationException
import java.io.IOException
import java.io.InputStream

class JSONSerializer {
    companion object {
        private val objectMapper: ObjectMapper = ObjectMapper()

        init {
            objectMapper.registerModule(Jdk8Module())
            objectMapper.registerModule(
                KotlinModule.Builder()
                    .withReflectionCacheSize(512)
                    .configure(KotlinFeature.NullToEmptyCollection, false)
                    .configure(KotlinFeature.NullToEmptyMap, false)
                    .configure(KotlinFeature.NullIsSameAsDefault, false)
                    .configure(KotlinFeature.SingletonSupport, false)
                    .configure(KotlinFeature.StrictNullChecks, false)
                    .build()
            )

            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)

            objectMapper.configOverride(Map::class.java).include =
                JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false)
            objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false)
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            objectMapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)

            objectMapper
                .registerModule(ParameterNamesModule())
                .registerModule(Jdk8Module())
                .registerModule(JavaTimeModule())
        }

        fun <T> serialize(obj: T): String {
            try {
                return objectMapper.writeValueAsString(obj)
            } catch (e: JsonProcessingException) {
                throw JsonSerializationException(e)
            }
        }

        fun <T> deserialize(json: String, clazz: Class<T>): T {
            try {
                return objectMapper.readValue(json, clazz)
            } catch (e: IOException) {
                throw JsonSerializationException(e)
            }
        }

        @Throws(IOException::class)
        fun <T> deserialize(json: InputStream, clazz: Class<T>): T {
            return objectMapper.readValue(json, clazz)
        }
    }
}
