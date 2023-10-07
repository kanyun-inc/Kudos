package com.kanyun.kudos.sample.test

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.google.gson.reflect.TypeToken
import com.kanyun.kudos.annotations.Kudos
import com.kanyun.kudos.gson.kudosGson
import com.kanyun.kudos.jackson.kudosObjectMapper
import kotlin.test.assertEquals
import org.junit.Test

class KudosTest {

    inline fun <reified T: Any> deserializeByJackson(string: String, expect: String) {
        val mapper = kudosObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS);
        try {
            val t: T = mapper.readValue(string, object: TypeReference<T>() {})
            assertEquals(t.toString(), expect)
        } catch (e: Exception) {
            assertEquals(e.toString(), expect)
        }
    }

    inline fun <reified T: Any> deserializeByGson(string: String, expect: String) {
        val gson = kudosGson()
        try {
            val t: T = gson.fromJson(string, object: TypeToken<T>() {}.type)
            assertEquals(t.toString(), expect)
        } catch (e: Exception) {
            assertEquals(e.toString(), expect)
        }
    }

    @Kudos
    data class User(val id: Long, val name: String, val age: Int = 10)

    fun gsonSample() {
        deserializeByGson<User>("""{}""", "java.lang.NullPointerException: Missing non-null field 'id'.")
        deserializeByGson<User>("""{"id": 10}""", "java.lang.NullPointerException: Missing non-null field 'name'.")
    }

    fun jacksonSample() {
        deserializeByJackson<User>("""{}""", "java.lang.NullPointerException: Missing non-null field 'id'.")
        deserializeByJackson<User>("""{"id": 10}""", "java.lang.NullPointerException: Missing non-null field 'name'.")
    }

    @Test
    fun test() {
        println("test Kudos.")
        gsonSample()
        jacksonSample()
    }
}
